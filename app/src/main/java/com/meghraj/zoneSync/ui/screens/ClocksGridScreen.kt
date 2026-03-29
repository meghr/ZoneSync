package com.meghraj.zoneSync.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.meghraj.zoneSync.models.TimezoneData
import com.meghraj.zoneSync.viewmodels.MainViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClocksGridScreen(
    viewModel: MainViewModel,
    timezones: List<TimezoneData>,
    onFabClick: () -> Unit
) {
    val referenceClockId by viewModel.referenceClockId.collectAsState()
    val referenceTime by viewModel.referenceTime.collectAsState()

    var showTimePicker by remember { mutableStateOf<TimezoneData?>(null) }
    var contextMenuTimezone by remember { mutableStateOf<TimezoneData?>(null) }
    
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        onMove = { from, to ->
            viewModel.reorderTimezones(from.index, to.index)
        }
    )

    if (timezones.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌍", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onFabClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(8.dp))
                    Text("Add Timezone", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            if (referenceClockId != null && referenceTime != null) {
                val tz = timezones.find { it.id == referenceClockId }
                if (tz != null) {
                    val zoneAbbr = tz.zoneId.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val timeStr = referenceTime!!.format(DateTimeFormatter.ofPattern("h:mm a"))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = androidx.compose.ui.text.buildAnnotatedString {
                                    append("Synced to ")
                                    withStyle(androidx.compose.ui.text.SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )) {
                                        append("${tz.cityName} ($zoneAbbr)")
                                    }
                                    append(" · $timeStr — tap any clock to adjust")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (timezones.size == 1) 1 else 2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(timezones, key = { _, item -> item.id }) { index, timezone ->
                    ReorderableItem(
                        state = reorderableState,
                        key = timezone.id
                    ) { isDragging ->
                        val (calculatedTime, isLive) = viewModel.getCalculatedTimeFor(timezone.id)
                        val isRef = referenceClockId == timezone.id

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                                    viewModel.removeTimezone(timezone)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        ) {
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = 300f)) + fadeIn()
                            ) {
                                ClockCard(
                                    timezone = timezone,
                                    calculatedTime = calculatedTime,
                                    isLive = isLive,
                                    isReference = isRef,
                                    onTap = {
                                        if (isRef) {
                                            showTimePicker = timezone
                                        } else {
                                            viewModel.setReferenceTime(timezone.id, calculatedTime)
                                        }
                                    },
                                    onLongPress = {
                                        contextMenuTimezone = timezone
                                    },
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = { },
                                        onDragStopped = { },
                                        interactionSource = remember { MutableInteractionSource() }
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    AddClockSlot(onClick = onFabClick)
                }
            }
        }
    }

    if (showTimePicker != null) {
        val tz = showTimePicker!!
        val (calcTime, _) = viewModel.getCalculatedTimeFor(tz.id)
        
        val timePickerState = rememberTimePickerState(
            initialHour = calcTime.hour,
            initialMinute = calcTime.minute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newTime = calcTime.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                        viewModel.setReferenceTime(tz.id, newTime)
                        showTimePicker = null
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = null }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (contextMenuTimezone != null) {
        val tz = contextMenuTimezone!!
        ModalBottomSheet(
            onDismissRequest = { contextMenuTimezone = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = tz.cityName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Set as Reference") },
                    modifier = Modifier.clickable {
                        val (calcTime, _) = viewModel.getCalculatedTimeFor(tz.id)
                        viewModel.setReferenceTime(tz.id, calcTime)
                        contextMenuTimezone = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        viewModel.removeTimezone(tz)
                        contextMenuTimezone = null
                    }
                )
            }
        }
    }
}

@Composable
fun AddClockSlot(onClick: () -> Unit) {
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
    )
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // keeps the dashed card somewhat square/card-shaped
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = stroke,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Timezone",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Add timezone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() }
    )
}
