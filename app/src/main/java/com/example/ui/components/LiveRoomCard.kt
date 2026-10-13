package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientSessionEntity
import com.example.data.model.RoomStatus
import com.example.data.model.RoomType
import com.example.data.model.SessionStatus
import com.example.data.model.SpaRoomEntity
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGold
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableContainer
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusCleaningContainer
import com.example.ui.theme.StatusCleaningPurple
import com.example.ui.theme.StatusFinishingSoonAmber
import com.example.ui.theme.StatusFinishingSoonContainer
import com.example.ui.theme.StatusInUseBlue
import com.example.ui.theme.StatusInUseContainer
import com.example.ui.theme.StatusSteamContainer
import com.example.ui.theme.StatusSteamTeal

@Composable
fun LiveRoomCard(
    room: SpaRoomEntity,
    activeSession: ClientSessionEntity?,
    currentTimeEpoch: Long,
    onStartTreatment: (String) -> Unit,
    onCompleteTreatment: (String) -> Unit,
    onAddOvertime: (String, Int) -> Unit,
    onCheckInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSteamBath = room.roomType == RoomType.STEAM_BATH.name
    val isCoupleRoom = room.roomType == RoomType.COUPLE.name

    // Calculate live timer metrics if in treatment
    var timeUsedFormatted = "0m 00s"
    var timeRemainingFormatted = "0m 00s"
    var progressFraction = 0f
    var isFinishingSoon = false
    var isOvertime = false

    if (activeSession != null && activeSession.status == SessionStatus.IN_TREATMENT.name && activeSession.startedAt != null) {
        val elapsedMs = (currentTimeEpoch - activeSession.startedAt).coerceAtLeast(0L)
        val totalAllowedMinutes = activeSession.totalDurationMinutes + activeSession.addedOvertimeMinutes
        val totalAllowedMs = totalAllowedMinutes * 60 * 1000L

        val elapsedSec = elapsedMs / 1000L
        val usedMins = elapsedSec / 60
        val usedSecs = elapsedSec % 60
        timeUsedFormatted = "${usedMins}m ${String.format("%02d", usedSecs)}s"

        val remainingMs = totalAllowedMs - elapsedMs
        if (remainingMs <= 0) {
            isOvertime = true
            val overtimeSec = (-remainingMs) / 1000L
            timeRemainingFormatted = "+${overtimeSec / 60}m ${String.format("%02d", overtimeSec % 60)}s OVERTIME"
            progressFraction = 1f
            isFinishingSoon = true
        } else {
            val remainingSec = remainingMs / 1000L
            val remMins = remainingSec / 60
            val remSecs = remainingSec % 60
            timeRemainingFormatted = "${remMins}m ${String.format("%02d", remSecs)}s"
            progressFraction = (elapsedMs.toFloat() / totalAllowedMs.toFloat()).coerceIn(0f, 1f)
            if (remMins < 15) {
                isFinishingSoon = true
            }
        }
    }

    val cardBorderColor = when {
        isFinishingSoon -> StatusFinishingSoonAmber
        activeSession != null -> SpaDeepForest
        room.status == RoomStatus.FREE.name -> StatusAvailableGreen.copy(alpha = 0.5f)
        else -> SpaCardBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_card_${room.id}")
            .border(
                width = if (isFinishingSoon) 2.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFinishingSoon) Color(0xFFFFFDF8) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Room Name & Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSteamBath -> StatusSteamContainer
                                    isCoupleRoom -> Color(0xFFFBE9E7)
                                    else -> SpaMist
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isSteamBath -> Icons.Default.HotTub
                                isCoupleRoom -> Icons.Default.Spa
                                else -> Icons.Default.MeetingRoom
                            },
                            contentDescription = null,
                            tint = when {
                                isSteamBath -> StatusSteamTeal
                                isCoupleRoom -> Color(0xFFD84315)
                                else -> SpaDeepForest
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )
                        Text(
                            text = when (room.roomType) {
                                RoomType.COUPLE.name -> "Couple Suite • Capacity 2"
                                RoomType.STEAM_BATH.name -> "Thermal Steam Lounge • Multi-guest"
                                else -> "Single Treatment Room"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }

                // Room Status Badge
                RoomStatusChip(
                    status = room.status,
                    isFinishingSoon = isFinishingSoon,
                    hasActiveSession = activeSession != null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Active Session Information or Free State
            if (activeSession != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFinishingSoon) StatusFinishingSoonContainer.copy(alpha = 0.5f) else SpaMist.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Client & Card Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SpaGoldDark,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = activeSession.cardNumber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (activeSession.isCouple && !activeSession.secondaryClientName.isNullOrBlank()) {
                                        "${activeSession.clientName} & ${activeSession.secondaryClientName}"
                                    } else {
                                        activeSession.clientName
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SpaDarkGreen
                                    )
                                )
                            }

                            // Steaming in-between badge if applicable
                            if (activeSession.hasSteamBetween) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StatusSteamContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.HotTub,
                                            contentDescription = null,
                                            tint = StatusSteamTeal,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "+15m Steam",
                                            style = MaterialTheme.typography.labelSmall.copy(color = StatusSteamTeal)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Therapist row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = SpaDeepForest,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Therapist: ${activeSession.therapistName}" +
                                        if (!activeSession.secondaryTherapistName.isNullOrBlank()) " & ${activeSession.secondaryTherapistName}" else "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SpaDeepForest,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Treatments line
                        Text(
                            text = "Treatments: ${activeSession.treatmentNames}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        // Timer & Progress Section (if treatment in progress)
                        if (activeSession.status == SessionStatus.IN_TREATMENT.name && activeSession.startedAt != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Used: $timeUsedFormatted",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isFinishingSoon) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = StatusFinishingSoonAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "Remaining: $timeRemainingFormatted",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFinishingSoon) StatusFinishingSoonAmber else SpaDeepForest
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isFinishingSoon) StatusFinishingSoonAmber else SpaDeepForest,
                                trackColor = Color(0xFFD6E2DE)
                            )

                            if (isFinishingSoon && !isOvertime) {
                                Text(
                                    text = "⚡ Almost Finished (< 15 mins) - Alert front desk & prepare turnover",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StatusFinishingSoonAmber,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        } else if (activeSession.status == SessionStatus.READY_FOR_ROOM.name || activeSession.status == SessionStatus.WAITING.name) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF9C4),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PhoneInTalk,
                                        contentDescription = null,
                                        tint = Color(0xFFF57F17),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Client assigned to room. Waiting for Therapist call to start timer...",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF5D4037))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reception Actions: Start, Complete, Add Overtime
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeSession.status != SessionStatus.IN_TREATMENT.name) {
                        Button(
                            onClick = { onStartTreatment(activeSession.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_treatment_button_${room.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Therapist Called: Start")
                        }
                    } else {
                        Button(
                            onClick = { onCompleteTreatment(activeSession.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("complete_treatment_button_${room.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusAvailableGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Therapist Called: Done")
                        }

                        OutlinedButton(
                            onClick = { onAddOvertime(activeSession.id, 10) },
                            modifier = Modifier.testTag("add_overtime_button_${room.id}"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+10m")
                        }
                    }
                }
            } else {
                // Room is Free or Cleaning
                if (room.status == RoomStatus.FREE.name) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Room Ready for Next Client",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = StatusAvailableGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Sanitized & Prepared",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }

                        Button(
                            onClick = onCheckInClick,
                            modifier = Modifier.testTag("check_in_to_room_button_${room.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check In")
                        }
                    }
                } else if (room.status == RoomStatus.CLEANING.name) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = StatusCleaningPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Turnover & Cleaning",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = StatusCleaningPurple,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "5-min buffer in progress",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onCheckInClick,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ready Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomStatusChip(
    status: String,
    isFinishingSoon: Boolean,
    hasActiveSession: Boolean
) {
    val (label, containerColor, contentColor) = when {
        isFinishingSoon -> Triple("Finishing Soon", StatusFinishingSoonContainer, StatusFinishingSoonAmber)
        hasActiveSession || status == RoomStatus.IN_USE.name -> Triple("In Session", StatusInUseContainer, StatusInUseBlue)
        status == RoomStatus.CLEANING.name -> Triple("Cleaning", StatusCleaningContainer, StatusCleaningPurple)
        status == RoomStatus.RESERVED.name -> Triple("Reserved", Color(0xFFEDE7F6), Color(0xFF512DA8))
        else -> Triple("Available", StatusAvailableContainer, StatusAvailableGreen)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(contentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )
        }
    }
}
