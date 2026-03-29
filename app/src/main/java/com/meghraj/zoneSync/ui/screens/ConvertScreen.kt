package com.meghraj.zoneSync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meghraj.zoneSync.viewmodels.MainViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(viewModel: MainViewModel) {
    val timezones by viewModel.timezones.collectAsState()
    var baseTime by remember { mutableStateOf(LocalDateTime.now()) }
    var baseTz by remember { mutableStateOf<String?>(null) }
    
    var showTimePicker by remember { mutableStateOf(false) }

    if (timezones.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Add timezones first to use Convert tool")
        }
        return
    }

    if (baseTz == null && timezones.isNotEmpty()) {
        baseTz = timezones.first().id
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Convert Time",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            onClick = { showTimePicker = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
               Column {
                   Text("Base Time (${timezones.find { it.id == baseTz }?.cityName ?: "Select"})", style = MaterialTheme.typography.labelMedium)
                   Text(baseTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm")), style = MaterialTheme.typography.titleLarge)
               }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(timezones.filter { it.id != baseTz }) { tz ->
                val baseZone = ZoneId.of(baseTz ?: "UTC")
                val zonedDateTime = baseTime.atZone(baseZone)
                val targetTime = zonedDateTime.withZoneSameInstant(tz.zoneId).toLocalDateTime()
                
                ListItem(
                    headlineContent = { Text(targetTime.format(DateTimeFormatter.ofPattern("HH:mm - MMM d"))) },
                    supportingContent = { Text(tz.cityName) },
                    leadingContent = { Text(tz.flagEmoji, style = MaterialTheme.typography.titleLarge) }
                )
            }
        }
    }
    
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = baseTime.hour,
            initialMinute = baseTime.minute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        baseTime = baseTime.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
