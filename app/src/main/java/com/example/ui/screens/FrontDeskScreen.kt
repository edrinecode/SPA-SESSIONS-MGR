package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ClientSessionEntity
import com.example.data.model.RoomStatus
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
import com.example.ui.theme.StatusFinishingSoonAmber
import com.example.ui.theme.StatusSteamTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    onSeatWaitingClient: (sessionId: String, roomId: String) -> Unit,
    onSendWaitingToSteam: (sessionId: String) -> Unit,
    onCancelWaitingSession: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mode switcher: "MINIMAL_SHEET" (Front Desk Form from paper sheet) vs "ROOMS_GRID"
    var viewMode by remember { mutableStateOf("MINIMAL_SHEET") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Dialog state for seating waiting client in a room
    var seatingSessionId by remember { mutableStateOf<String?>(null) }

    val waitingSessions = remember(activeSessions) {
        activeSessions.filter { it.status == SessionStatus.WAITING.name }
            .sortedBy { it.checkInTime }
    }

    val preSteamSessions = remember(activeSessions) {
        activeSessions.filter { it.status == SessionStatus.IN_PRE_STEAM.name }
    }

    val inTreatmentSessions = remember(activeSessions) {
        activeSessions.filter { it.status == SessionStatus.IN_TREATMENT.name || it.status == SessionStatus.READY_FOR_ROOM.name }
    }

    val filteredRooms = remember(rooms, selectedFilter) {
        when (selectedFilter) {
            "SINGLE" -> rooms.filter { it.roomType == RoomType.SINGLE.name }
            "COUPLE" -> rooms.filter { it.roomType == RoomType.COUPLE.name }
            "STEAM" -> rooms.filter { it.roomType == RoomType.STEAM_BATH.name }
            else -> rooms
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedDate = remember(currentTimeEpoch) { dateFormatter.format(Date(currentTimeEpoch)) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Manager Header: POSITIVE EMOTIONS FITNESS AND SPA
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaDarkGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "POSITIVE EMOTIONS FITNESS AND SPA",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "FRONT DESK SESSION MANAGER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SpaGoldDark,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            // View mode selector
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SpaDeepForest,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SpaGoldDark.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (viewMode == "MINIMAL_SHEET") SpaGoldDark else Color.Transparent,
                                        modifier = Modifier
                                            .clickable { viewMode = "MINIMAL_SHEET" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.TableChart,
                                                contentDescription = "Sheet View",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Form Sheet",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (viewMode == "ROOMS_GRID") SpaGoldDark else Color.Transparent,
                                        modifier = Modifier
                                            .clickable { viewMode = "ROOMS_GRID" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.GridView,
                                                contentDescription = "Rooms Grid",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Suites Grid",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Manager on Duty & Real-time KPI Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date: $formattedDate",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFECEFF1))
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SpaDeepForest
                                ) {
                                    Text(
                                        text = "Active Clients: ${activeSessions.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (waitingSessions.isNotEmpty()) Color(0xFFE65100) else SpaDeepForest
                                ) {
                                    Text(
                                        text = "Waiting: ${waitingSessions.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Smart Reception Availability & Wait Time Banner
            item {
                WaitTimeBanner(estimate = waitTimeEstimate)
            }

            // 3. WAITING CLIENTS QUEUE SECTION (Critical fix for client visibility & individual waiting times!)
            if (waitingSessions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFFFB74D), RoundedCornerShape(16.dp))
                            .testTag("waiting_clients_section"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Reception Waiting Lounge (${waitingSessions.size} clients)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBF360C)
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFE0B2)
                                ) {
                                    Text(
                                        text = "Queue FIFO",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // List of all waiting clients with their specific calculated wait time
                            waitingSessions.forEachIndexed { index, session ->
                                val queuePosition = index + 1
                                val waitInfo = waitTimeEstimate.clientWaitEstimates[session.id]
                                val waitMinutes = waitInfo?.estimatedWaitMinutes ?: (queuePosition * 15)
                                val targetRoom = waitInfo?.targetRoomName ?: session.roomName

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE0B2)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Queue Position Badge
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFE65100),
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = "#$queuePosition IN LINE",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Card number
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = SpaMist
                                                ) {
                                                    Text(
                                                        text = "Card ${session.cardNumber}",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = SpaDarkGreen
                                                        )
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    text = session.clientName + (if (session.isCouple && !session.secondaryClientName.isNullOrBlank()) " & ${session.secondaryClientName}" else ""),
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            // Client's Individual Wait Time Badge
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (waitMinutes == 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (waitMinutes == 0) StatusAvailableGreen else Color(0xFFFFB74D)
                                                )
                                            ) {
                                                Text(
                                                    text = if (waitMinutes == 0) "Room Ready!" else "Est. Wait: ~$waitMinutes min",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (waitMinutes == 0) StatusAvailableGreen else Color(0xFFE65100)
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Treatments: ${session.treatmentNames} • Therapist: ${session.therapistName}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                        )

                                        Text(
                                            text = "Allocated Suite: $targetRoom • Time In: ${timeFormatter.format(Date(session.checkInTime))}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Front Desk Actions for Waiting Client
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { onSendWaitingToSteam(session.id) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusSteamTeal),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.HotTub, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Send to Steam Bath", style = MaterialTheme.typography.labelSmall)
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = { seatingSessionId = session.id },
                                                colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Seat in Room", style = MaterialTheme.typography.labelSmall)
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = { onCancelWaitingSession(session.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancel Wait", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Pre-Session Steam Bath Lounge Section
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

            // 5. Minimal Front Desk Form View (The exact paper sheet requested by user!)
            if (viewMode == "MINIMAL_SHEET") {
                item {
                    MinimalFrontDeskSheet(
                        activeSessions = activeSessions,
                        currentTimeEpoch = currentTimeEpoch,
                        rooms = rooms,
                        onStartTreatment = onStartTreatment,
                        onCompleteTreatment = onCompleteTreatment,
                        onAddOvertime = onAddOvertime,
                        onSeatInRoom = { sessionId -> seatingSessionId = sessionId }
                    )
                }
            } else {
                // 6. Suites Grid View with Live Progress Bars
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

        // Seating Dialog: Seat Waiting Client into an Available Room
        if (seatingSessionId != null) {
            val sessionToSeat = activeSessions.find { it.id == seatingSessionId }
            Dialog(onDismissRequest = { seatingSessionId = null }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Seat Client into Room",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )
                        Text(
                            text = "Client: ${sessionToSeat?.clientName ?: ""} (Card ${sessionToSeat?.cardNumber ?: ""})",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Choose room to assign:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val availableRooms = rooms.filter { it.roomType != RoomType.STEAM_BATH.name }
                        availableRooms.forEach { room ->
                            val isFree = room.status == RoomStatus.FREE.name
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isFree) SpaMist else Color(0xFFF5F5F5),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isFree) StatusAvailableGreen else Color(0xFFE0E0E0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSeatWaitingClient(sessionToSeat!!.id, room.id)
                                        seatingSessionId = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MeetingRoom,
                                            contentDescription = null,
                                            tint = if (isFree) StatusAvailableGreen else StatusFinishingSoonAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(room.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Text(
                                                text = if (isFree) "Available Now" else "Occupied (${room.status})",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (isFree) StatusAvailableGreen else Color(0xFFC62828)
                                                )
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onSeatWaitingClient(sessionToSeat!!.id, room.id)
                                            seatingSessionId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("Select", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(onClick = { seatingSessionId = null }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Digitized "POSITIVE EMOTIONS FITNESS AND SPA - FRONT DESK FORM"
 * Minimal table view corresponding to the physical sheet with columns:
 * CLIENT'S NAME | TREATMENT | THERAPIST | ROOM ALLOCATED | TIME IN | TIME OUT | ACTIONS
 */
@Composable
fun MinimalFrontDeskSheet(
    activeSessions: List<ClientSessionEntity>,
    currentTimeEpoch: Long,
    rooms: List<SpaRoomEntity>,
    onStartTreatment: (String) -> Unit,
    onCompleteTreatment: (String) -> Unit,
    onAddOvertime: (String, Int) -> Unit,
    onSeatInRoom: (String) -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("minimal_front_desk_sheet"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FRONT DESK SESSION LOG",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )
                    Text(
                        text = "Real-time records for all active client visits",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SpaMist
                ) {
                    Text(
                        text = "Total Sessions: ${activeSessions.size}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = SpaCardBorder)

            if (activeSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Spa,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No active client sessions at front desk.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Text(
                            text = "Click '+ Check In Client' to register a new client.",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaDeepForest)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeSessions.forEach { session ->
                        val timeInStr = timeFormatter.format(Date(session.checkInTime))
                        val isStarted = session.startedAt != null
                        val isWaiting = session.status == SessionStatus.WAITING.name
                        val isPreSteam = session.status == SessionStatus.IN_PRE_STEAM.name
                        val isReadyForRoom = session.status == SessionStatus.READY_FOR_ROOM.name

                        val totalAllowedMinutes = session.totalDurationMinutes + session.addedOvertimeMinutes
                        val elapsedSeconds = if (session.startedAt != null) {
                            (currentTimeEpoch - session.startedAt) / 1000L
                        } else 0L
                        val remainingSeconds = (totalAllowedMinutes * 60L - elapsedSeconds).coerceAtLeast(0L)
                        val remainingMins = remainingSeconds / 60
                        val progress = if (totalAllowedMinutes > 0 && isStarted) {
                            (elapsedSeconds.toFloat() / (totalAllowedMinutes * 60f)).coerceIn(0f, 1f)
                        } else 0f

                        val expectedEndTimeStr = if (session.startedAt != null) {
                            timeFormatter.format(Date(session.startedAt + totalAllowedMinutes * 60 * 1000L))
                        } else {
                            "Pending Call"
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isWaiting -> Color(0xFFFFFDE7)
                                isPreSteam -> Color(0xFFE0F2F1)
                                isReadyForRoom -> Color(0xFFE8F5E9)
                                else -> Color(0xFFFAFAFA)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    isWaiting -> Color(0xFFFFE082)
                                    isPreSteam -> Color(0xFF80CBC4)
                                    isReadyForRoom -> StatusAvailableGreen
                                    else -> SpaCardBorder
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Row 1: Card #, Client Name, Room Allocated, Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SpaGoldDark,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = session.cardNumber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = session.clientName + (if (session.isCouple && !session.secondaryClientName.isNullOrBlank()) " & ${session.secondaryClientName}" else ""),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SpaDarkGreen
                                            )
                                        )
                                    }

                                    // Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (session.status) {
                                            SessionStatus.WAITING.name -> Color(0xFFFFF3E0)
                                            SessionStatus.IN_PRE_STEAM.name -> Color(0xFFE0F2F1)
                                            SessionStatus.READY_FOR_ROOM.name -> Color(0xFFE8F5E9)
                                            SessionStatus.IN_TREATMENT.name -> Color(0xFFE3F2FD)
                                            else -> Color(0xFFEEEEEE)
                                        }
                                    ) {
                                        Text(
                                            text = when (session.status) {
                                                SessionStatus.WAITING.name -> "Waiting in Lounge"
                                                SessionStatus.IN_PRE_STEAM.name -> "Pre-Steam Bath"
                                                SessionStatus.READY_FOR_ROOM.name -> "Ready for Call"
                                                SessionStatus.IN_TREATMENT.name -> "In Treatment ($remainingMins m left)"
                                                else -> session.status
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when (session.status) {
                                                    SessionStatus.WAITING.name -> Color(0xFFE65100)
                                                    SessionStatus.IN_PRE_STEAM.name -> StatusSteamTeal
                                                    SessionStatus.READY_FOR_ROOM.name -> StatusAvailableGreen
                                                    SessionStatus.IN_TREATMENT.name -> Color(0xFF1565C0)
                                                    else -> Color.DarkGray
                                                }
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Row 2: Grid-like details (Treatment | Therapist | Room | Time In | Time Out)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = "TREATMENT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = session.treatmentNames,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            maxLines = 2
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "THERAPIST",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = session.therapistName + (if (session.secondaryTherapistName != null) " & ${session.secondaryTherapistName}" else ""),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(0.9f)) {
                                        Text(
                                            text = "ROOM ALLOCATED",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = if (isWaiting) "Waiting Lounge" else session.roomName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isWaiting) Color(0xFFE65100) else SpaDarkGreen
                                            )
                                        )
                                    }

                                    Column(modifier = Modifier.weight(0.7f)) {
                                        Text(
                                            text = "TIME IN",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = timeInStr,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(0.8f)) {
                                        Text(
                                            text = "TIME OUT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = expectedEndTimeStr,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (isStarted) SpaDarkGreen else Color.Gray)
                                        )
                                    }
                                }

                                // Live progress bar if in treatment
                                if (isStarted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (remainingMins < 10) StatusFinishingSoonAmber else SpaDeepForest,
                                        trackColor = Color(0xFFE0E0E0)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Front Desk Action Buttons for session
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isWaiting) {
                                        Button(
                                            onClick = { onSeatInRoom(session.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Assign Room", style = MaterialTheme.typography.labelSmall)
                                        }
                                    } else if (!isStarted) {
                                        Button(
                                            onClick = { onStartTreatment(session.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusAvailableGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call: Start Treatment", style = MaterialTheme.typography.labelSmall)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onAddOvertime(session.id, 15) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("+15m Steam / Overtime", style = MaterialTheme.typography.labelSmall)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { onCompleteTreatment(session.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call: Treatment Done", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = SpaCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Signature & Manager info from the physical sheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MANAGER ON DUTY: Front Desk Active",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpaDarkGreen
                    )
                )

                Text(
                    text = "NUMBER OF CLIENTS: ${activeSessions.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpaDarkGreen
                    )
                )
            }
        }
    }
}
