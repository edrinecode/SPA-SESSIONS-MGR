package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.RoomStatus
import com.example.data.model.RoomType
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.data.model.TreatmentEntity
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusFinishingSoonAmber
import com.example.ui.theme.StatusSteamTeal

@Composable
fun CheckInDialog(
    isOpen: Boolean,
    suggestedCardNumber: String,
    preselectedRoom: SpaRoomEntity?,
    rooms: List<SpaRoomEntity>,
    therapists: List<TherapistEntity>,
    treatments: List<TreatmentEntity>,
    settings: BranchSettingsEntity?,
    onDismiss: () -> Unit,
    onOpenAddTherapist: (() -> Unit)? = null,
    onConfirmCheckIn: (
        clientName: String,
        clientPhone: String,
        isCouple: Boolean,
        secondaryClientName: String?,
        selectedTreatments: List<TreatmentEntity>,
        therapist: TherapistEntity,
        secondaryTherapist: TherapistEntity?,
        room: SpaRoomEntity,
        hasSteamBetween: Boolean,
        hasPreSteam: Boolean,
        cardNumber: String,
        notes: String
    ) -> Unit
) {
    if (!isOpen) return

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var isCouple by remember { mutableStateOf(preselectedRoom?.roomType == RoomType.COUPLE.name) }
    var secondaryClientName by remember { mutableStateOf("") }
    var cardNumber by remember(suggestedCardNumber) { mutableStateOf(suggestedCardNumber) }

    val selectedTreatments = remember { mutableStateListOf<TreatmentEntity>() }
    var hasSteamBetween by remember { mutableStateOf(false) }
    var hasPreSteam by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    // Sorted therapists by rotation order
    val sortedTherapists = remember(therapists) {
        therapists.sortedBy { it.orderIndex }
    }

    // Stable selection using therapist IDs so timer recompositions never wipe user selection
    var selectedTherapistId by remember {
        mutableStateOf(
            sortedTherapists.firstOrNull { it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday }?.id
                ?: sortedTherapists.firstOrNull()?.id
        )
    }

    var selectedSecondaryTherapistId by remember {
        mutableStateOf(
            sortedTherapists.filter { it.id != selectedTherapistId }
                .firstOrNull { it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday }?.id
                ?: sortedTherapists.filter { it.id != selectedTherapistId }.firstOrNull()?.id
        )
    }

    // When therapists list loads or changes, ensure an initial selection is made if none was selected
    LaunchedEffect(sortedTherapists) {
        if (selectedTherapistId == null && sortedTherapists.isNotEmpty()) {
            selectedTherapistId = sortedTherapists.firstOrNull {
                it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday
            }?.id ?: sortedTherapists.first().id
        }
        if (selectedSecondaryTherapistId == null && sortedTherapists.size > 1) {
            selectedSecondaryTherapistId = sortedTherapists.firstOrNull {
                it.id != selectedTherapistId && it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday
            }?.id ?: sortedTherapists.firstOrNull { it.id != selectedTherapistId }?.id
        }
    }

    val selectedTherapist = sortedTherapists.find { it.id == selectedTherapistId } ?: sortedTherapists.firstOrNull()
    val selectedSecondaryTherapist = sortedTherapists.find { it.id == selectedSecondaryTherapistId }

    // Room Selection
    val filteredRooms = remember(rooms, isCouple) {
        if (isCouple) {
            rooms.filter { it.roomType == RoomType.COUPLE.name }
        } else {
            rooms.filter { it.roomType == RoomType.SINGLE.name }
        }
    }

    var selectedRoomId by remember {
        mutableStateOf(
            preselectedRoom?.id
                ?: filteredRooms.firstOrNull { it.status == RoomStatus.FREE.name }?.id
                ?: filteredRooms.firstOrNull()?.id
        )
    }

    LaunchedEffect(filteredRooms) {
        if (selectedRoomId == null && filteredRooms.isNotEmpty()) {
            selectedRoomId = preselectedRoom?.id
                ?: filteredRooms.firstOrNull { it.status == RoomStatus.FREE.name }?.id
                ?: filteredRooms.first().id
        }
    }

    val selectedRoom = filteredRooms.find { it.id == selectedRoomId }
        ?: filteredRooms.firstOrNull { it.status == RoomStatus.FREE.name }
        ?: filteredRooms.firstOrNull()

    // Total Duration Calculation
    val steamExtra = if (hasSteamBetween) (settings?.steamBetweenExtraMinutes ?: 15) else 0
    val totalTreatmentDuration = selectedTreatments.sumOf { it.durationMinutes } + steamExtra
    val totalCost = selectedTreatments.sumOf { it.price }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 740.dp)
                .testTag("check_in_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Client Front Desk Check-In",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )
                        Text(
                            text = "Assign card, therapist in rotation, treatments & room",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = SpaCardBorder)

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Guest Type & Card Number
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it },
                                label = { Text("Card Number") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = SpaGoldDark) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("check_in_card_number_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Guest Type Switcher
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SpaMist,
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FilterChip(
                                        selected = !isCouple,
                                        onClick = { isCouple = false },
                                        label = { Text("Single") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = isCouple,
                                        onClick = { isCouple = true },
                                        label = { Text("Couple") },
                                        leadingIcon = { Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Client Name & Contact
                    item {
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = {
                                clientName = it
                                if (errorMessage != null) errorMessage = null
                            },
                            label = { Text("Client Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("check_in_client_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (isCouple) {
                        item {
                            OutlinedTextField(
                                value = secondaryClientName,
                                onValueChange = { secondaryClientName = it },
                                label = { Text("Partner / Secondary Guest Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("check_in_secondary_client_name"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            label = { Text("Phone Number (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 3. Treatment Selection (Single or Multi-treatments)
                    item {
                        Text(
                            text = "Select Treatments (Single or Multi-Treatment Bundle)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total Duration is calculated automatically.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }

                    items(treatments) { treatment ->
                        val isSelected = selectedTreatments.any { it.id == treatment.id }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SpaMist else Color(0xFFFAFAFA),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SpaDeepForest else SpaCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedTreatments.removeAll { it.id == treatment.id }
                                        hasSteamBetween = selectedTreatments.any { it.hasSteamBetween }
                                    } else {
                                        selectedTreatments.add(treatment)
                                        if (treatment.hasSteamBetween) hasSteamBetween = true
                                    }
                                    if (errorMessage != null) errorMessage = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked && !isSelected) {
                                                selectedTreatments.add(treatment)
                                                if (treatment.hasSteamBetween) hasSteamBetween = true
                                            } else if (!checked && isSelected) {
                                                selectedTreatments.removeAll { it.id == treatment.id }
                                                hasSteamBetween = selectedTreatments.any { it.hasSteamBetween }
                                            }
                                            if (errorMessage != null) errorMessage = null
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = treatment.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "${treatment.category} • ${treatment.durationMinutes} mins • $${treatment.price.toInt()}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Steam Bath Options
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF0FBF8),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.HotTub, contentDescription = null, tint = StatusSteamTeal)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Steam Bath Between Treatments (+15 min)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Adds 15 minutes of steaming to total treatment time",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = hasSteamBetween,
                                        onCheckedChange = { hasSteamBetween = it },
                                        modifier = Modifier.testTag("steam_between_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.HotTub, contentDescription = null, tint = SpaDeepForest)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Pre-Session Steam Bath First",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Take client to steam room first before entering treatment room",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = hasPreSteam,
                                        onCheckedChange = { hasPreSteam = it },
                                        modifier = Modifier.testTag("pre_steam_switch")
                                    )
                                }
                            }
                        }
                    }

                    // 5. Therapist Assignment (Queue Order)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Assign Therapist (Daily Rotation Order)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SpaDarkGreen
                                    )
                                )
                                Text(
                                    text = "Auto-suggests next therapist in line. Tap any therapist to assign.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                            if (onOpenAddTherapist != null) {
                                IconButton(onClick = onOpenAddTherapist) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Therapist", tint = SpaDeepForest)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (sortedTherapists.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF3E0),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "No therapists configured for this branch yet.",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    )
                                    Text(
                                        text = "Add therapists in the Therapists tab or click below.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                    )
                                    if (onOpenAddTherapist != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = onOpenAddTherapist,
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Therapist to Roster")
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sortedTherapists.forEachIndexed { index, therapist ->
                                    val isNextUp = therapist.id == sortedTherapists.firstOrNull {
                                        it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday
                                    }?.id
                                    val isSelected = selectedTherapist?.id == therapist.id

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SpaMist else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) SpaDeepForest else SpaCardBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedTherapistId = therapist.id
                                                if (errorMessage != null) errorMessage = null
                                            }
                                            .testTag("checkin_therapist_${therapist.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isNextUp) SpaGoldDark else if (isSelected) SpaDeepForest else Color(0xFFECEFF1),
                                                    contentColor = if (isNextUp || isSelected) Color.White else Color.DarkGray
                                                ) {
                                                    Text(
                                                        text = "#${therapist.orderIndex}",
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = therapist.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = SpaDarkGreen
                                                            )
                                                        )
                                                        if (isNextUp) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = Color(0xFFE8F5E9)
                                                            ) {
                                                                Text(
                                                                    text = "NEXT UP",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        color = StatusAvailableGreen,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 10.sp
                                                                    ),
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = when (therapist.status) {
                                                                TherapistStatus.AVAILABLE.name -> Color(0xFFE8F5E9)
                                                                TherapistStatus.IN_ROOM.name -> Color(0xFFE3F2FD)
                                                                TherapistStatus.BOOKED.name -> Color(0xFFFFF3E0)
                                                                else -> Color(0xFFEEEEEE)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = when (therapist.status) {
                                                                    TherapistStatus.IN_ROOM.name -> "In Room: ${therapist.currentRoomName ?: "Treating"}"
                                                                    TherapistStatus.BOOKED.name -> "Pre-Booked"
                                                                    TherapistStatus.ON_BREAK.name -> "On Break"
                                                                    else -> "Available"
                                                                },
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = when (therapist.status) {
                                                                        TherapistStatus.AVAILABLE.name -> StatusAvailableGreen
                                                                        TherapistStatus.IN_ROOM.name -> Color(0xFF1565C0)
                                                                        TherapistStatus.BOOKED.name -> Color(0xFFE65100)
                                                                        else -> Color.DarkGray
                                                                    },
                                                                    fontWeight = FontWeight.Medium
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                            )
                                                        }

                                                        if (therapist.specialties.isNotBlank()) {
                                                            Text(
                                                                text = "• ${therapist.specialties}",
                                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Radio selection circle
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) SpaDeepForest else Color(0xFFEEEEEE)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = "Selected Therapist",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Secondary Therapist for Couples
                    if (isCouple) {
                        item {
                            Text(
                                text = "Second Therapist for Couple Suite",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose secondary therapist assisting the couple session.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                sortedTherapists.filter { it.id != selectedTherapist?.id }.forEach { therapist ->
                                    val isSelected = selectedSecondaryTherapistId == therapist.id
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) SpaMist else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) SpaDeepForest else SpaCardBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedSecondaryTherapistId = therapist.id
                                                if (errorMessage != null) errorMessage = null
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFECEFF1)
                                                ) {
                                                    Text(
                                                        text = "#${therapist.orderIndex}",
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(therapist.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            }

                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = SpaDeepForest)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 7. Room Allocation
                    item {
                        Text(
                            text = if (isCouple) "Select Couple Room" else "Select Single Room",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val allRoomsOccupied = filteredRooms.isNotEmpty() && filteredRooms.none { it.status == RoomStatus.FREE.name }
                        if (allRoomsOccupied) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF8E1),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "All suites are currently occupied. Client will be checked into Reception Waiting Lounge and notified as rooms become free.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBF360C), fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }

                        if (filteredRooms.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF3E0),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No ${if (isCouple) "couple" else "single"} rooms found for this branch. Please add one in Catalog.",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE65100))
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                filteredRooms.forEach { room ->
                                    val isFree = room.status == RoomStatus.FREE.name
                                    val isSelected = selectedRoom?.id == room.id

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) SpaMist else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) SpaDeepForest else SpaCardBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedRoomId = room.id
                                                if (errorMessage != null) errorMessage = null
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
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
                                                        text = if (isFree) "Available Now" else room.status.lowercase().replace('_', ' '),
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (isFree) StatusAvailableGreen else Color(0xFFC62828)
                                                        )
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = SpaDeepForest)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 8. Notes
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Requests / Client Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Error alert
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Summary & Confirm Action
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SpaMist,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Total Time: $totalTreatmentDuration min",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                            )
                            Text(
                                text = "${selectedTreatments.size} treatments • Therapist: ${selectedTherapist?.name ?: "None"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                            )
                        }

                        Button(
                            onClick = {
                                if (clientName.isBlank()) {
                                    errorMessage = "Please enter client name"
                                    return@Button
                                }
                                if (selectedTreatments.isEmpty()) {
                                    errorMessage = "Please select at least one treatment"
                                    return@Button
                                }

                                val therapist = selectedTherapist ?: sortedTherapists.firstOrNull() ?: therapists.firstOrNull()
                                if (therapist == null) {
                                    errorMessage = "Please select an assigned therapist from the list above (or add one to today's roster)."
                                    return@Button
                                }

                                if (isCouple && selectedSecondaryTherapist == null && sortedTherapists.size > 1) {
                                    errorMessage = "Please select a second therapist for the couple visit."
                                    return@Button
                                }

                                val room = selectedRoom ?: filteredRooms.firstOrNull() ?: rooms.firstOrNull()
                                if (room == null) {
                                    errorMessage = "Please select an assigned room or add one in Catalog."
                                    return@Button
                                }

                                onConfirmCheckIn(
                                    clientName,
                                    clientPhone,
                                    isCouple,
                                    secondaryClientName,
                                    selectedTreatments.toList(),
                                    therapist,
                                    if (isCouple) selectedSecondaryTherapist else null,
                                    room,
                                    hasSteamBetween,
                                    hasPreSteam,
                                    cardNumber,
                                    notes
                                )
                            },
                            modifier = Modifier.testTag("confirm_check_in_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm & Check In")
                        }
                    }
                }
            }
        }
    }
}
