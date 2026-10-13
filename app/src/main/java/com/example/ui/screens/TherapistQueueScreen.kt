package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.ui.components.TherapistQueueItem
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableGreen

@Composable
fun TherapistQueueScreen(
    therapists: List<TherapistEntity>,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onStatusToggle: (String, TherapistStatus) -> Unit,
    onOpenAddTherapist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedTherapists = therapists.sortedBy { it.orderIndex }
    val availableTherapists = sortedTherapists.filter { it.status == TherapistStatus.AVAILABLE.name }
    val nextInLine = availableTherapists.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Manager Rotation Notice Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("therapist_roster_explanation_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SpaMist.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(SpaDeepForest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Reorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Therapist Turn Order",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SpaDarkGreen
                                    )
                                )
                                Text(
                                    text = "Fair rotation arranged by manager",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAddTherapist,
                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Therapist")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Fair Rotation Rule: When a therapist completes a treatment session, they automatically move to the bottom of the line to let the next therapist take the walk-in.\n• Pre-Booked Appointments: If a therapist is requested/booked for a set time slot, their turn order is temporarily held.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF37474F), lineHeight = 18.sp)
                    )
                }
            }
        }

        // Highlight Next In Line Card
        if (nextInLine != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF9E6),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SpaGoldDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SpaGoldDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "NEXT THERAPIST UP FOR WALK-IN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SpaGoldDark,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = nextInLine.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SpaDarkGreen
                                    )
                                )
                                Text(
                                    text = "Specialties: ${nextInLine.specialties}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "READY",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = StatusAvailableGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Therapist Rotation List
        itemsIndexed(sortedTherapists, key = { _, t -> t.id }) { index, therapist ->
            val isFirstAvailable = therapist.id == nextInLine?.id
            TherapistQueueItem(
                therapist = therapist,
                isFirstInLine = isFirstAvailable,
                onMoveUp = { onMoveUp(therapist.id) },
                onMoveDown = { onMoveDown(therapist.id) },
                onStatusToggle = { newStatus -> onStatusToggle(therapist.id, newStatus) }
            )
        }
    }
}
