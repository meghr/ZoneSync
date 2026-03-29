package com.meghraj.zoneSync.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meghraj.zoneSync.models.TimezoneData
import com.meghraj.zoneSync.utils.TimezoneHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimezoneSheet(
    onDismiss: () -> Unit,
    onTimezoneSelected: (TimezoneData) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    
    var allZones by remember { mutableStateOf<List<TimezoneData>>(emptyList()) }
    var filteredZones by remember { mutableStateOf<List<TimezoneData>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val zones = TimezoneHelper.getAllTimezones()
            allZones = zones
            filteredZones = zones
        }
    }

    LaunchedEffect(searchQuery, allZones) {
        filteredZones = if (searchQuery.isBlank()) {
            allZones
        } else {
            allZones.filter { 
                it.cityName.contains(searchQuery, ignoreCase = true) || 
                it.countryName.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search city or country...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredZones) { zone ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onTimezoneSelected(zone)
                        },
                        headlineContent = { Text(zone.cityName) },
                        supportingContent = { Text(zone.countryName) },
                        leadingContent = { 
                            Text(
                                text = zone.flagEmoji, 
                                style = MaterialTheme.typography.titleLarge
                            ) 
                        },
                        trailingContent = {
                            val now = LocalDateTime.now(zone.zoneId)
                            val formatter = DateTimeFormatter.ofPattern("HH:mm")
                            val offset = zone.zoneId.rules.getOffset(now.atZone(zone.zoneId).toInstant())
                            Text(
                                text = "${now.format(formatter)} (UTC${if(offset.totalSeconds>=0) "+" else ""}${offset.id.replace("Z", "+00:00")})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }
            }
        }
    }
}
