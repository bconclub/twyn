package com.twin.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.twin.app.ui.ChatScreen

class ChatActivity : ComponentActivity() {

    private var micGranted = false
    private val micPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            micGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dest = intent?.data
        val voiceMode = dest?.getQueryParameter("voice") == "1"
        val memoryMode = dest?.host == "memory"
        micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (voiceMode && !micGranted) micPermission.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            ChatScreen(
                startVoice = voiceMode,
                startMemory = memoryMode,
                requestMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) },
                onClose = { finish() },
            )
        }
    }
}
