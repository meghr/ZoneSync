package com.meghraj.zoneSync.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meghraj.zoneSync.models.TimezoneData
import com.meghraj.zoneSync.viewmodels.MainViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MeetingPlannerScreen(viewModel: MainViewModel) {
    val timezones by viewModel.timezones.collectAsState()
    
    // We maintain a selected "anchor" time in UTC
    var selectedUtcTime by remember { mutableStateOf(LocalDateTime.now(ZoneId.of("UTC"))) }
    
    if (timezones.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Add timezones first to use Meeting Planner")
        }
        return
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Meeting Planner",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Drag the timeline to sync time across zones",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(timezones) { tz ->
                val localTime = selectedUtcTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(tz.zoneId).toLocalDateTime()
                TimelineRow(
                    timezone = tz,
                    calculatedTime = localTime,
                    onTimeDragged = { dragOffsetHours ->
                        val newUtcTime = selectedUtcTime.plusSeconds((dragOffsetHours * 3600).toLong())
                        selectedUtcTime = newUtcTime
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TimelineRow(
    timezone: TimezoneData,
    calculatedTime: LocalDateTime,
    onTimeDragged: (Float) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${timezone.flagEmoji} ${timezone.cityName}", fontWeight = FontWeight.Bold)
            Text(
                text = calculatedTime.format(formatter) + " " + (if (calculatedTime.hour in 9..17) "🧑‍💻" else if (calculatedTime.hour >= 22 || calculatedTime.hour <= 7) "😴" else "☕"),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // 24 Hour Bar Canvas
        val hourOfDay = calculatedTime.hour + calculatedTime.minute / 60f
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // rough standard width conversion
                        val widthStr = size.width.toFloat()
                        // total 24 hours width
                        val hoursDragged = -(dragAmount.x / widthStr) * 24f 
                        onTimeDragged(hoursDragged)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Draw Business Hours 9-18 (green) and Sleep Hours 22-7 (red)
                val hourWidth = width / 24f
                
                // Sleep early (0-7)
                drawRect(
                    color = Color(0x66B71C1C), // Dark Red with alpha
                    topLeft = Offset(0f, 0f),
                    size = Size(7 * hourWidth, height)
                )
                
                // Business (9-18)
                drawRect(
                    color = Color(0x661B5E20), // Green with alpha
                    topLeft = Offset(9 * hourWidth, 0f),
                    size = Size(9 * hourWidth, height)
                )
                
                // Sleep late (22-24)
                drawRect(
                    color = Color(0x66B71C1C),
                    topLeft = Offset(22 * hourWidth, 0f),
                    size = Size(2 * hourWidth, height)
                )
                
                // Draw current pointer
                val pointerX = hourOfDay * hourWidth
                drawLine(
                    color = Color.White,
                    start = Offset(pointerX, 0f),
                    end = Offset(pointerX, height),
                    strokeWidth = 4.dp.toPx()
                )
            }
        }
    }
}
