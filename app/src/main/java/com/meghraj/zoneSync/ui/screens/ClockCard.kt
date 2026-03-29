package com.meghraj.zoneSync.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meghraj.zoneSync.models.TimezoneData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockCard(
    timezone: TimezoneData,
    calculatedTime: LocalDateTime,
    isLive: Boolean,
    isReference: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatterTime = DateTimeFormatter.ofPattern("HH:mm")
    val formatterAmPm = DateTimeFormatter.ofPattern("a")
    val formatterDate = DateTimeFormatter.ofPattern("E, d MMM")

    val timeStr = calculatedTime.format(formatterTime)
    val amPmStr = calculatedTime.format(formatterAmPm)
    val dateStr = calculatedTime.format(formatterDate)

    val offset = timezone.zoneId.rules.getOffset(calculatedTime.atZone(timezone.zoneId).toInstant())
    // Ensure clean offset format like +1:00 instead of UTC+1:00
    var offsetId = offset.id.replace("Z", "+0:00")
    if (!offsetId.startsWith("+") && !offsetId.startsWith("-")) {
        offsetId = "+$offsetId"
    }

    val zoneAbbr = timezone.zoneId.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    val borderColor = if (isReference) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderStroke = if (isReference) BorderStroke(2.dp, borderColor) else null

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Ensure roughly square for grid matching screenshot 2
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(24.dp)) else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Top row: Flag + Pill Offset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = timezone.flagEmoji, fontSize = 24.sp)
                
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = offsetId,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // Middle info
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = timezone.cityName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedText(text = timeStr)
                    
                    AnimatedVisibility(
                        visible = isLive,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        BlinkingColon()
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$amPmStr · $zoneAbbr",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // The bottom right dot
                    SyncDot(isActive = isReference)
                }
            }
        }
    }
}

@Composable
fun AnimatedText(text: String) {
    Row {
        text.forEach { char ->
            if (char == ':') {
                Text(":", fontSize = 36.sp, fontWeight = FontWeight.Normal)
            } else {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    },
                    label = "digit_animation"
                ) { charAt ->
                    Text(
                        text = charAt.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun BlinkingColon() {
}

@Composable
fun SyncDot(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "SyncDot")
    
    val alphaAnim = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "SyncDotAlpha"
        ).value
    } else {
        1f
    }
    
    val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color.copy(alpha = alphaAnim), CircleShape)
    )
}
