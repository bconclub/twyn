package com.twin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.twin.app.TwinApp
import com.twin.app.voice.Speech

private val Bg = Color(0xFF111111)
private val Surface2 = Color(0xFF1D1D1F)
private val Accent = Color(0xFF7C6CFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    startVoice: Boolean,
    requestMic: () -> Unit,
    onClose: () -> Unit,
    vm: ChatViewModel = viewModel(),
) {
    val context = LocalContext.current
    val prefs = TwinApp.instance.prefs
    val messages by vm.messages.collectAsState()
    val busy by vm.busy.collectAsState()
    var input by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(!prefs.configured) }
    val speech = remember { Speech(context) }
    val focus = remember { FocusRequester() }
    val listState = rememberLazyListState()

    fun startListening() {
        if (!speech.available) return
        listening = true
        speech.start(
            onPartial = { input = it },
            onFinal = { text ->
                listening = false
                input = ""
                if (text.isNotBlank()) vm.send(text)
            },
            onError = { listening = false },
        )
    }

    LaunchedEffect(Unit) {
        if (startVoice && prefs.configured) startListening() else focus.requestFocus()
    }
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    DisposableEffect(Unit) { onDispose { speech.stop() } }

    if (showSettings) {
        SettingsDialog(onDone = { showSettings = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Bg)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("TWYN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showSettings = true }) { Text("⋯", color = Color.Gray, fontSize = 20.sp) }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            if (prefs.interviewed) "Talk to yourself." else "First run: your twin will interview you to become you. Say hi.",
                            color = Color.Gray, fontSize = 14.sp,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }
                items(messages) { m ->
                    val isUser = m.role == "user"
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    ) {
                        Box(
                            Modifier
                                .widthIn(max = 300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isUser) Accent else Surface2)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                m.text.ifBlank { if (m.streaming) "…" else "" },
                                color = Color.White, fontSize = 15.sp, lineHeight = 21.sp,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focus),
                    placeholder = { Text(if (listening) "Listening…" else "Message", color = Color.Gray) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface2,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    maxLines = 4,
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (input.isBlank()) {
                            if (listening) { speech.stop(); listening = false } else { requestMic(); startListening() }
                        } else {
                            vm.send(input); input = ""
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (listening) Color(0xFFE5484D) else Accent,
                    ),
                ) {
                    Text(if (input.isBlank()) "🎙" else "↑", fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(onDone: () -> Unit) {
    val prefs = TwinApp.instance.prefs
    var url by remember { mutableStateOf(prefs.serverUrl) }
    var token by remember { mutableStateOf(prefs.token) }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Connect your twin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Server URL") })
                OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("Token") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                prefs.serverUrl = url
                prefs.token = token
                if (prefs.configured) onDone()
            }) { Text("Save") }
        },
    )
}
