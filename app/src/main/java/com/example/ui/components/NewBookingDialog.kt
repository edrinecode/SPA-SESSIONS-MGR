package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.RoomType
import com.example.data.model.TherapistEntity
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingDialog(
    isOpen: Boolean,
    therapists: List<TherapistEntity>,
    onDismiss: () -> Unit,
    onConfirmBooking: (BookingEntity) -> Unit
) {
    if (!isOpen) return

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var treatmentSummary by remember { mutableStateOf("Deep Tissue Massage (60m)") }
    var durationMinutesText by remember { mutableStateOf("60") }
    var targetRoomType by remember { mutableStateOf(RoomType.SINGLE.name) }
    var selectedTherapist by remember(therapists) { mutableStateOf(therapists.firstOrNull()) }
    var timeSlotHour by remember { mutableStateOf("14") }
    var timeSlotMinute by remember { mutableStateOf("00") }
    var notes by remember { mutableStateOf("") }

    var therapistExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_booking_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Therapist Booking",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )
                        Text(
                            text = "Reserve a therapist slot and room in advance",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SpaCardBorder)

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Therapist Selector
                ExposedDropdownMenuBox(
                    expanded = therapistExpanded,
                    onExpandedChange = { therapistExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTherapist?.name ?: "Select Therapist",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Book Specific Therapist *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = therapistExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = therapistExpanded,
                        onDismissRequest = { therapistExpanded = false }
                    ) {
                        therapists.forEach { therapist ->
                            DropdownMenuItem(
                                text = { Text("${therapist.name} (${therapist.status})") },
                                onClick = {
                                    selectedTherapist = therapist
                                    therapistExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = timeSlotHour,
                        onValueChange = { timeSlotHour = it.take(2) },
                        label = { Text("Hour (24h)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = timeSlotMinute,
                        onValueChange = { timeSlotMinute = it.take(2) },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = treatmentSummary,
                    onValueChange = { treatmentSummary = it },
                    label = { Text("Treatment Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = targetRoomType == RoomType.SINGLE.name,
                        onClick = { targetRoomType = RoomType.SINGLE.name },
                        label = { Text("Single Room") }
                    )
                    FilterChip(
                        selected = targetRoomType == RoomType.COUPLE.name,
                        onClick = { targetRoomType = RoomType.COUPLE.name },
                        label = { Text("Couple Suite") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (clientName.isBlank() || selectedTherapist == null) return@Button

                        val cal = Calendar.getInstance()
                        val h = timeSlotHour.toIntOrNull() ?: 14
                        val m = timeSlotMinute.toIntOrNull() ?: 0
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, m)
                        cal.set(Calendar.SECOND, 0)

                        val booking = BookingEntity(
                            id = "book_${UUID.randomUUID().toString().take(8)}",
                            branchId = "",
                            clientName = clientName.trim(),
                            clientPhone = clientPhone.trim(),
                            therapistId = selectedTherapist?.id ?: "",
                            therapistName = selectedTherapist?.name ?: "",
                            targetRoomType = targetRoomType,
                            scheduledTimeEpoch = cal.timeInMillis,
                            treatmentSummary = treatmentSummary.trim(),
                            durationMinutes = durationMinutesText.toIntOrNull() ?: 60,
                            status = BookingStatus.SCHEDULED.name,
                            notes = notes
                        )
                        onConfirmBooking(booking)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_booking_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Booking")
                }
            }
        }
    }
}
