package com.twin.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.app.PendingIntent
import android.service.quicksettings.TileService

class TwinTileService : TileService() {
    override fun onClick() {
        val intent = Intent(this, ChatActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("twin://chat")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val pi = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            startActivityAndCollapse(pi)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
