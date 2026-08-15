package com.twin.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twin.app.TwinApp
import com.twin.app.data.SseEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val text: String, val streaming: Boolean = false)

class ChatViewModel : ViewModel() {
    private val app get() = TwinApp.instance
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    fun send(text: String) {
        if (text.isBlank() || _busy.value) return
        val mode = if (app.prefs.interviewed) "chat" else "interview"
        _messages.value = _messages.value + ChatMessage("user", text) +
            ChatMessage("assistant", "", streaming = true)
        _busy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            app.api.chat(text, mode) { ev ->
                when (ev) {
                    is SseEvent.Delta -> {
                        sb.append(ev.text)
                        updateLast(sb.toString(), streaming = true)
                    }
                    is SseEvent.Done -> {
                        val finalText = ev.text.ifBlank { sb.toString() }
                        updateLast(finalText, streaming = false)
                        app.prefs.lastTwinMessage = finalText
                        if (!app.prefs.interviewed) app.prefs.interviewed = true
                        _busy.value = false
                    }
                    is SseEvent.Error -> {
                        updateLast("[${ev.message}]", streaming = false)
                        _busy.value = false
                    }
                    is SseEvent.Tool -> Unit
                }
            }
        }
    }

    private fun updateLast(text: String, streaming: Boolean) {
        val list = _messages.value.toMutableList()
        if (list.isNotEmpty() && list.last().role == "assistant") {
            list[list.size - 1] = ChatMessage("assistant", text, streaming)
            _messages.value = list
        }
    }
}
