package com.meghraj.zoneSync.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.TextAlign
import androidx.glance.unit.ColorProvider
import com.meghraj.zoneSync.data.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import java.time.ZonedDateTime

class ZoneSyncWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStoreManager = DataStoreManager(context)
        val timezones = dataStoreManager.savedTimezones.firstOrNull() ?: emptyList()
        val displayZones = timezones.take(4)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x991B1B22)) // ~60% transparency
                    .padding(8.dp)
                    .clickable(actionRunCallback<RefreshAction>())
            ) {
                if (displayZones.isEmpty()) {
                    Text(
                        text = "No clocks added.",
                        style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp)
                    )
                } else {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        displayZones.forEach { tz ->
                            val currentZdt = ZonedDateTime.now(tz.zoneId)
                            val bitmap = drawAnalogClock(currentZdt)
                            
                            val offset = tz.zoneId.rules.getOffset(currentZdt.toInstant())
                            var offsetStr = offset.id.replace("Z", "+0:00")
                            if (!offsetStr.startsWith("+") && !offsetStr.startsWith("-")) offsetStr = "+$offsetStr"

                            Column(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    provider = ImageProvider(bitmap),
                                    contentDescription = "Analog Clock for ${tz.cityName}",
                                    modifier = GlanceModifier.size(64.dp)
                                )
                                Spacer(modifier = GlanceModifier.height(8.dp))
                                Text(
                                    text = tz.countryName.uppercase(),
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "UTC$offsetStr",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF7A5BFF)),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun drawAnalogClock(currentZdt: ZonedDateTime): android.graphics.Bitmap {
        val size = 240
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        val cx = size / 2f
        val cy = size / 2f
        val radius = size / 2f - 10f

        val hourObj = currentZdt.hour
        val isDay = hourObj in 6..17

        val bgColor = if (isDay) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        val fgColor = if (isDay) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        val accentColor = android.graphics.Color.parseColor("#7A5BFF")

        // Draw background
        paint.color = bgColor
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius, paint)

        // Draw border
        paint.color = accentColor
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawCircle(cx, cy, radius, paint)

        // Draw numbers 1 to 12
        paint.color = fgColor
        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = 36f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        // Adjust for vertical text centering
        val textOffset = (paint.descent() + paint.ascent()) / 2f
        
        for (i in 1..12) {
            val angle = Math.PI / 6 * i - Math.PI / 2
            val numberRadius = radius - 35f
            val x = (cx + numberRadius * Math.cos(angle)).toFloat()
            val y = (cy + numberRadius * Math.sin(angle)).toFloat() - textOffset
            canvas.drawText(i.toString(), x, y, paint)
        }

        val hour12 = hourObj % 12
        val minute = currentZdt.minute

        // Draw hour hand
        val hourAngle = Math.PI * (hour12 + minute / 60f) / 6 - Math.PI / 2
        paint.color = accentColor
        paint.strokeWidth = 16f
        paint.strokeCap = android.graphics.Paint.Cap.ROUND
        canvas.drawLine(
            cx,
            cy,
            (cx + (radius * 0.45) * Math.cos(hourAngle)).toFloat(),
            (cy + (radius * 0.45) * Math.sin(hourAngle)).toFloat(),
            paint
        )

        // Draw minute hand
        val minAngle = Math.PI * minute / 30 - Math.PI / 2
        paint.color = fgColor
        paint.strokeWidth = 10f
        canvas.drawLine(
            cx,
            cy,
            (cx + (radius * 0.7) * Math.cos(minAngle)).toFloat(),
            (cy + (radius * 0.7) * Math.sin(minAngle)).toFloat(),
            paint
        )

        // Draw center dot
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = fgColor
        canvas.drawCircle(cx, cy, 14f, paint)

        return bitmap
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        ZoneSyncWidget().updateAll(context)
    }
}

class ZoneSyncWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZoneSyncWidget()
}
