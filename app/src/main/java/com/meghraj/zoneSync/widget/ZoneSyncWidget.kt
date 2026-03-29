package com.meghraj.zoneSync.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import androidx.glance.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.layout.Row
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.GlanceModifier

class ZoneSyncWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E26))
                    .padding(16.dp)
            ) {
                Text(
                    text = "ZoneSync Clocks",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold
                    )
                )
                // In a real widget, you'd observe DataStore manually with StateDefinition
                // or flow, for simplicity we just render a placeholder.
                Row {
                    Text("Widget Live Time Updates using GlanceStateDefinition (Placeholder)", style = TextStyle(color = ColorProvider(Color.LightGray)))
                }
            }
        }
    }
}

class ZoneSyncWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZoneSyncWidget()
}
