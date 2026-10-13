package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BranchEntity
import com.example.data.model.RoomStatus
import com.example.data.model.RoomType
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.data.model.TreatmentEntity
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import java.util.UUID

@Composable
fun AddTherapistDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TherapistEntity) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var specialties by remember { mutableStateOf("Swedish, Deep Tissue") }
    var phone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("add_therapist_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Spa Therapist",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SpaCardBorder)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Therapist Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("therapist_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = specialties,
                    onValueChange = { specialties = it },
                    label = { Text("Specialties / Modalities") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Internal Phone / Extension") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val therapist = TherapistEntity(
                            id = "th_${UUID.randomUUID().toString().take(8)}",
                            branchId = "",
                            name = name.trim(),
                            specialties = specialties.trim(),
                            phone = phone.trim(),
                            status = TherapistStatus.AVAILABLE.name
                        )
                        onConfirm(therapist)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_therapist_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Therapist to Daily Roster")
                }
            }
        }
    }
}

@Composable
fun AddRoomDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (SpaRoomEntity) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf(RoomType.SINGLE.name) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("add_room_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Spa Treatment Room",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SpaCardBorder)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Room Name / Number *") },
                    placeholder = { Text("e.g. Room 5 (Jasmine Single)") },
                    leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("room_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Room Configuration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = roomType == RoomType.SINGLE.name,
                        onClick = { roomType = RoomType.SINGLE.name },
                        label = { Text("Single (1)") }
                    )
                    FilterChip(
                        selected = roomType == RoomType.COUPLE.name,
                        onClick = { roomType = RoomType.COUPLE.name },
                        label = { Text("Couple (2)") }
                    )
                    FilterChip(
                        selected = roomType == RoomType.STEAM_BATH.name,
                        onClick = { roomType = RoomType.STEAM_BATH.name },
                        label = { Text("Steam Bath") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val capacity = when (roomType) {
                            RoomType.COUPLE.name -> 2
                            RoomType.STEAM_BATH.name -> 6
                            else -> 1
                        }
                        val room = SpaRoomEntity(
                            id = "room_${UUID.randomUUID().toString().take(8)}",
                            branchId = "",
                            name = name.trim(),
                            roomType = roomType,
                            capacity = capacity,
                            status = RoomStatus.FREE.name
                        )
                        onConfirm(room)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_room_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Room to Branch")
                }
            }
        }
    }
}

@Composable
fun AddTreatmentDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TreatmentEntity) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Massage") }
    var durationText by remember { mutableStateOf("60") }
    var priceText by remember { mutableStateOf("70.0") }
    var description by remember { mutableStateOf("") }
    var hasSteamBetween by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("add_treatment_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Spa Treatment",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SpaCardBorder)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Treatment Name *") },
                    leadingIcon = { Icon(Icons.Default.Spa, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("treatment_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duration (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Includes Steam Bath Interval")
                    Switch(checked = hasSteamBetween, onCheckedChange = { hasSteamBetween = it })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val duration = durationText.toIntOrNull() ?: 60
                        val price = priceText.toDoubleOrNull() ?: 50.0
                        val treatment = TreatmentEntity(
                            id = "tr_${UUID.randomUUID().toString().take(8)}",
                            branchId = "",
                            name = name.trim(),
                            category = category.trim(),
                            durationMinutes = duration,
                            price = price,
                            description = description.trim(),
                            hasSteamBetween = hasSteamBetween
                        )
                        onConfirm(treatment)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_treatment_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Treatment")
                }
            }
        }
    }
}

@Composable
fun BranchDialog(
    isOpen: Boolean,
    branches: List<BranchEntity>,
    currentBranchId: String,
    onSelectBranch: (String) -> Unit,
    onAddBranch: (BranchEntity) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var isAddingNew by remember { mutableStateOf(false) }
    var newBranchName by remember { mutableStateOf("") }
    var newBranchAddress by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("branch_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAddingNew) "Add Spa Branch" else "Select Spa Branch",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpaDarkGreen
                        )
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SpaCardBorder)

                if (!isAddingNew) {
                    Text("Active Branches:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))

                    branches.forEach { branch ->
                        val isSelected = branch.id == currentBranchId
                        Button(
                            onClick = {
                                onSelectBranch(branch.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) SpaDeepForest else Color(0xFFF0F4F2),
                                contentColor = if (isSelected) Color.White else SpaDarkGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(branch.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { isAddingNew = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8ECEB), contentColor = SpaDarkGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add New Branch")
                    }
                } else {
                    OutlinedTextField(
                        value = newBranchName,
                        onValueChange = { newBranchName = it },
                        label = { Text("Branch Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newBranchAddress,
                        onValueChange = { newBranchAddress = it },
                        label = { Text("Address / Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (newBranchName.isBlank()) return@Button
                            val branch = BranchEntity(
                                id = "branch_${UUID.randomUUID().toString().take(8)}",
                                name = newBranchName.trim(),
                                address = newBranchAddress.trim()
                            )
                            onAddBranch(branch)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save & Switch to Branch")
                    }
                }
            }
        }
    }
}
