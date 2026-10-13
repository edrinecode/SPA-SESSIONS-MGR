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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.repository.WaitTimeEstimate
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableContainer
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusFinishingSoonAmber
import com.example.ui.theme.StatusFinishingSoonContainer
import com.example.ui.theme.StatusSteamContainer
import com.example.ui.theme.StatusSteamTeal

@Composable
fun WaitTimeBanner(
    estimate: WaitTimeEstimate,
    modifier: Modifier = Modifier
) {
    if (estimate.areAllRoomsBusy) {
        // High visibility alert banner: All rooms full
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("wait_time_full_banner")
                .border(1.5.dp, StatusFinishingSoonAmber, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusFinishingSoonContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusFinishingSoonAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "All Treatment Rooms Occupied",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                        )
                        Text(
                            text = "Walk-in clients will have a waiting queue.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D4037))
                        )
                    }

                    // Large Wait Time Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusFinishingSoonAmber,
                        contentColor = Color.White
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "EST. WAIT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "~${estimate.minutesUntilEarliestRoomFree} MIN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detail row: Which room and therapist frees up soonest
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = StatusFinishingSoonAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Earliest room opening: ",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                            )
                            Text(
                                text = estimate.earliestRoomFreeName ?: "Active Session",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                            )
                            Text(
                                text = " (with ${estimate.earliestRoomTherapistName ?: "Therapist"})",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Steam Bath Recommendation
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.HotTub,
                                contentDescription = null,
                                tint = StatusSteamTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💡 Front Desk Advice: Offer 15-min Steam Bath relaxation while waiting!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusSteamTeal
                                )
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Normal ready banner
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("wait_time_ready_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SpaMist.copy(alpha = 0.7f))
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StatusAvailableContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusAvailableGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "${estimate.freeRoomsCount} of ${estimate.totalRooms} Rooms Available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaDarkGreen
                            )
                        )
                        Text(
                            text = "Walk-in clients can be seated immediately",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }

                // Next Therapist Chip
                if (estimate.nextTherapistInTurn != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Next In Line: ",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                            Text(
                                text = estimate.nextTherapistInTurn.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpaDeepForest
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
