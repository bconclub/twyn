package com.twin.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.twin.app.ChatActivity
import com.twin.app.TwinApp

class TwinWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TwinWidget()
}

class TwinWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val last = TwinApp.instance.prefs.lastTwinMessage
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF111111), Color(0xFF111111)))
                        .cornerRadius(20.dp)
                        .padding(14.dp)
                        .clickable(actionStartActivity(chatIntent(context, voice = false))),
                ) {
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            "TWYN",
                            style = TextStyle(
                                color = ColorProvider(Color.White, Color.White),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Box(
                            modifier = GlanceModifier
                                .background(ColorProvider(Color(0xFF7C6CFF), Color(0xFF7C6CFF)))
                                .cornerRadius(16.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable(actionStartActivity(chatIntent(context, voice = true))),
                        ) {
                            Text(
                                "🎙 Talk",
                                style = TextStyle(color = ColorProvider(Color.White, Color.White), fontSize = 12.sp),
                            )
                        }
                    }
                    Spacer(GlanceModifier.height(8.dp))
                    Text(
                        last.ifBlank { "Tap to talk to yourself." },
                        style = TextStyle(color = ColorProvider(Color(0xFFB5B5B5), Color(0xFFB5B5B5)), fontSize = 13.sp),
                        maxLines = 3,
                    )
                }
            }
        }
    }

    private fun chatIntent(context: Context, voice: Boolean): Intent =
        Intent(context, ChatActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(if (voice) "twin://chat?voice=1" else "twin://chat")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
