package com.meghraj.zoneSync.ui.screens

import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meghraj.zoneSync.models.TimezoneData
import com.meghraj.zoneSync.viewmodels.MainViewModel
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showAlarmDialog by remember { mutableStateOf(false) }
    
    val timezones by viewModel.timezones.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("ZoneSync", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) 
                },
                actions = {
                    IconButton(
                        onClick = { showAlarmDialog = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = "Set Alarm", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Clocks") },
                    label = { Text("Clocks") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Meeting") },
                    label = { Text("Meeting") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SyncAlt, contentDescription = "Convert") },
                    label = { Text("Convert") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ClocksGridScreen(
                    viewModel = viewModel,
                    timezones = timezones,
                    onFabClick = { showAddSheet = true }
                )
                1 -> MeetingPlannerScreen(viewModel = viewModel)
                2 -> ConvertScreen(viewModel = viewModel)
            }
        }
    }

    if (showAddSheet) {
        AddTimezoneSheet(
            onDismiss = { showAddSheet = false },
            onTimezoneSelected = { tz ->
                viewModel.addTimezone(tz)
                showAddSheet = false
            }
        )
    }

    if (showAlarmDialog) {
        AlarmSetupDialog(
            timezones = timezones,
            onDismiss = { showAlarmDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSetupDialog(
    timezones: List<TimezoneData>,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTz by remember { mutableStateOf(timezones.firstOrNull()) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Smart Alarm") },
        text = {
            Column {
                if (timezones.isEmpty()) {
                    Text("Please add a timezone to your clocks grid first.")
                } else {
                    val safeTz = selectedTz ?: timezones.first()
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "${safeTz.flagEmoji} ${safeTz.cityName}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Timezone") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            timezones.forEach { tz ->
                                DropdownMenuItem(
                                    text = { Text("${tz.flagEmoji} ${tz.cityName}") },
                                    onClick = {
                                        selectedTz = tz
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "What time do you want the alarm to ring natively in ${safeTz.cityName}?", 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                }
            }
        },
        confirmButton = {
            if (timezones.isNotEmpty()) {
                Button(onClick = {
                    val tz = selectedTz ?: timezones.first()
                    val nowInTz = ZonedDateTime.now(tz.zoneId)
                    var target = nowInTz
                        .withHour(timePickerState.hour)
                        .withMinute(timePickerState.minute)
                        .withSecond(0)
                        .withNano(0)
                    
                    if (target.isBefore(nowInTz)) {
                        target = target.plusDays(1)
                    }
                    
                    val localZdt = target.withZoneSameInstant(ZoneId.systemDefault())
                    
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, localZdt.hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, localZdt.minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "ZoneSync: ${tz.cityName}")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                    }
                    context.startActivity(intent)
                    onDismiss()
                }) {
                    Text("Set Local Alarm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
