package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientSessionEntity
import com.example.data.model.RoomType
import com.example.data.model.SessionStatus
import com.example.data.model.SpaRoomEntity
import com.example.data.repository.WaitTimeEstimate
import com.example.ui.components.LiveRoomCard
import com.example.ui.components.WaitTimeBanner
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusSteamContainer
import com.example.ui.theme.StatusSteamTeal

@Composable
fun FrontDeskScreen(
    rooms: List<SpaRoomEntity>,
    activeSessions: List<ClientSessionEntity>,
    currentTimeEpoch: Long,
    waitTimeEstimate: WaitTimeEstimate,
    onOpenCheckIn: (SpaRoomEntity?) -> Unit,
    onStartTreatment: (String) -> Unit,
    onCompleteTreatment: (String) -> Unit,
    onAddOvertime: (String, Int) -> Unit,
    onTransitionPreSteam: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val preSteamSessions = remember(activeSessions) {
        activeSessions.filter { it.status == SessionStatus.IN_PRE_STEAM.name }
    }

    val filteredRooms = remember(rooms, selectedFilter) {
        when (selectedFilter) {
            "SINGLE" -> rooms.filter { it.roomType == RoomType.SINGLE.name }
            "COUPLE" -> rooms.filter { it.roomType == RoomType.COUPLE.name }
            "STEAM" -> rooms.filter { it.roomType == RoomType.STEAM_BATH.name }
            else -> rooms
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Smart Reception Availability & Wait Time Banner
            item {
                WaitTimeBanner(estimate = waitTimeEstimate)
            }

            // 2. Pre-Session Steam Bath Section (if clients are currently in steam lounge)
            if (preSteamSessions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF80CBC4), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HotTub, contentDescription = null, tint = StatusSteamTeal)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Thermal Steam Lounge (${preSteamSessions.size} relaxing)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSteamTeal
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "Pre-Treatment Steaming",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(color = StatusSteamTeal)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            preSteamSessions.forEach { session ->
                                val elapsedSec = if (session.preSteamStartedAt != null) {
                                    (currentTimeEpoch - session.preSteamStartedAt) / 1000L
                                } else 0L
                                val elapsedMins = elapsedSec / 60

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = SpaGoldDark,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        session.cardNumber,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    session.clientName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Text(
                                                text = "In Steam: ${elapsedMins}m / ${session.preSteamDurationMinutes}m target • Target Room: ${session.roomName}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                            )
                                        }

                                        Button(
                                            onClick = { onTransitionPreSteam(session.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Ready for Room", style = MaterialTheme.typography.labelMedium)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Rooms Filter Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Treatment Suites & Rooms (${filteredRooms.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedFilter == "SINGLE",
                            onClick = { selectedFilter = "SINGLE" },
                            label = { Text("Single", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedFilter == "COUPLE",
                            onClick = { selectedFilter = "COUPLE" },
                            label = { Text("Couple", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // 4. Live Rooms List with Live Timers
            items(filteredRooms, key = { it.id }) { room ->
                val sessionForRoom = activeSessions.find { it.roomId == room.id }
                LiveRoomCard(
                    room = room,
                    activeSession = sessionForRoom,
                    currentTimeEpoch = currentTimeEpoch,
                    onStartTreatment = onStartTreatment,
                    onCompleteTreatment = onCompleteTreatment,
                    onAddOvertime = onAddOvertime,
                    onCheckInClick = { onOpenCheckIn(room) }
                )
            }
        }

        // Floating Action Button for Quick Check-In
        ExtendedFloatingActionButton(
            onClick = { onOpenCheckIn(null) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Check In Client", fontWeight = FontWeight.Bold) },
            containerColor = SpaDeepForest,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("floating_check_in_button")
        )
    }
}
