package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableContainer
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusFinishingSoonAmber
import com.example.ui.theme.StatusFinishingSoonContainer
import com.example.ui.theme.StatusInUseBlue
import com.example.ui.theme.StatusInUseContainer

@Composable
fun TherapistQueueItem(
    therapist: TherapistEntity,
    isFirstInLine: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onStatusToggle: (TherapistStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = therapist.status == TherapistStatus.AVAILABLE.name
    val isInRoom = therapist.status == TherapistStatus.IN_ROOM.name
    val isBooked = therapist.status == TherapistStatus.BOOKED.name
    val isOnBreak = therapist.status == TherapistStatus.ON_BREAK.name

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("therapist_queue_item_${therapist.id}")
            .border(
                width = if (isFirstInLine && isAvailable) 2.dp else 1.dp,
                color = if (isFirstInLine && isAvailable) SpaGoldDark else SpaCardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFirstInLine && isAvailable) Color(0xFFFFFDF5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Order index & Next Up badge + Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFirstInLine && isAvailable) SpaGoldDark else SpaMist
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${therapist.orderIndex}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isFirstInLine && isAvailable) Color.White else SpaDeepForest
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = therapist.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )

                        if (isFirstInLine && isAvailable) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SpaGoldDark,
                                contentColor = Color.White
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "NEXT IN TURN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = therapist.specialties,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isAvailable -> StatusAvailableContainer
                                isInRoom -> StatusInUseContainer
                                isBooked -> StatusFinishingSoonContainer
                                else -> Color(0xFFECEFF1)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isAvailable -> Icons.Default.CheckCircle
                                        isInRoom -> Icons.Default.MeetingRoom
                                        isBooked -> Icons.Default.EventNote
                                        else -> Icons.Default.PauseCircle
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isAvailable -> StatusAvailableGreen
                                        isInRoom -> StatusInUseBlue
                                        isBooked -> StatusFinishingSoonAmber
                                        else -> Color.DarkGray
                                    },
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        isInRoom -> "In Room: ${therapist.currentRoomName ?: "Treating"}"
                                        isBooked -> "Pre-Booked / Scheduled"
                                        isOnBreak -> "On Break"
                                        else -> "Available"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = when {
                                            isAvailable -> StatusAvailableGreen
                                            isInRoom -> StatusInUseBlue
                                            isBooked -> StatusFinishingSoonAmber
                                            else -> Color.DarkGray
                                        }
                                    )
                                )
                            }
                        }

                        // Completed sessions count
                        Text(
                            text = "${therapist.completedSessionsToday} completed today",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                }
            }

            // Right: Manager reorder controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("therapist_move_up_${therapist.id}")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up in Queue", tint = SpaDeepForest)
                }

                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("therapist_move_down_${therapist.id}")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down in Queue", tint = SpaDeepForest)
                }
            }
        }
    }
}
