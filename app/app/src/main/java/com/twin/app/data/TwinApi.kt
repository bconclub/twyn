package com.twin.app.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class MemoryFile(val path: String, val bytes: Int, val mtime: String)

sealed class SseEvent {
    data class Delta(val text: String) : SseEvent()
    data class Tool(val name: String) : SseEvent()
    data class Done(val text: String) : SseEvent()
    data class Error(val message: String) : SseEvent()
}

class TwinApi(private val prefs: Prefs) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // SSE: no read timeout
        .build()

    private val rest = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun auth() = "Bearer ${prefs.token}"

    private fun memoryUrl(path: String): String {
        val encoded = path.split("/").joinToString("/") {
            URLEncoder.encode(it, "UTF-8").replace("+", "%20")
        }
        return "${prefs.serverUrl}/memory/$encoded"
    }

    fun listMemory(): List<MemoryFile> {
        val req = Request.Builder()
            .url("${prefs.serverUrl}/memory")
            .header("Authorization", auth())
            .get()
            .build()
        rest.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val arr = JSONArray(text)
            return buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(MemoryFile(o.getString("path"), o.optInt("bytes"), o.optString("mtime")))
                }
            }
        }
    }

    fun getMemory(path: String): String {
        val req = Request.Builder()
            .url(memoryUrl(path))
            .header("Authorization", auth())
            .get()
            .build()
        rest.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            return text
        }
    }

    fun putMemory(path: String, content: String) {
        val body = JSONObject().put("content", content).toString()
            .toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(memoryUrl(path))
            .header("Authorization", auth())
            .put(body)
            .build()
        rest.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
        }
    }

    fun deleteMemory(path: String) {
        val req = Request.Builder()
            .url(memoryUrl(path))
            .header("Authorization", auth())
            .delete()
            .build()
        rest.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
        }
    }

    /** Blocking SSE stream; call from a background dispatcher. */
    fun chat(message: String, mode: String, onEvent: (SseEvent) -> Unit) {
        val body = JSONObject()
            .put("message", message)
            .put("mode", mode)
            .toString()
            .toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("${prefs.serverUrl}/chat")
            .header("Authorization", "Bearer ${prefs.token}")
            .header("Accept", "text/event-stream")
            .post(body)
            .build()
        try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    onEvent(SseEvent.Error("HTTP ${resp.code}"))
                    return
                }
                val source = resp.body?.source() ?: run {
                    onEvent(SseEvent.Error("empty body")); return
                }
                var event = ""
                while (true) {
                    val line = source.readUtf8Line() ?: break
                    when {
                        line.startsWith("event: ") -> event = line.removePrefix("event: ")
                        line.startsWith("data: ") -> {
                            val data = JSONObject(line.removePrefix("data: "))
                            when (event) {
                                "delta" -> onEvent(SseEvent.Delta(data.optString("text")))
                                "tool" -> onEvent(SseEvent.Tool(data.optString("name")))
                                "done" -> onEvent(SseEvent.Done(data.optString("text")))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onEvent(SseEvent.Error(e.message ?: "network error"))
        }
    }
}
