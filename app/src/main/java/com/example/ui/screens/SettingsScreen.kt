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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.BranchEntity
import com.example.data.model.BranchSettingsEntity
import com.example.ui.theme.SpaCardBorder
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusSteamTeal

@Composable
fun SettingsScreen(
    currentBranch: BranchEntity?,
    branches: List<BranchEntity>,
    settings: BranchSettingsEntity?,
    onOpenBranchDialog: () -> Unit,
    onSaveSettings: (BranchSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSettings = settings ?: BranchSettingsEntity(currentBranch?.id ?: "")

    var cardStartText by remember(activeSettings) { mutableStateOf(activeSettings.cardStartNumber.toString()) }
    var cardEndText by remember(activeSettings) { mutableStateOf(activeSettings.cardEndNumber.toString()) }
    var cardPrefixText by remember(activeSettings) { mutableStateOf(activeSettings.cardPrefix) }
    var steamBetweenText by remember(activeSettings) { mutableStateOf(activeSettings.steamBetweenExtraMinutes.toString()) }
    var defaultPreSteamText by remember(activeSettings) { mutableStateOf(activeSettings.defaultPreSteamMinutes.toString()) }
    var cleaningBufferText by remember(activeSettings) { mutableStateOf(activeSettings.roomCleaningBufferMinutes.toString()) }
    var autoSuggestNext by remember(activeSettings) { mutableStateOf(activeSettings.autoSuggestNextTherapist) }

    var saveFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Branch Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("active_branch_settings_card")
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpaMist),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = SpaDeepForest, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentBranch?.name ?: "Spa Branch",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                                )
                                Text(
                                    text = currentBranch?.address ?: "Branch Location",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenBranchDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Switch Branch")
                        }
                    }
                }
            }
        }

        // Card Number Range Settings
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("card_number_range_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = SpaGoldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Client Card Number Range Settings",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                        )
                    }
                    Text(
                        text = "Checked-in clients are assigned cards depending on this number range.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cardPrefixText,
                            onValueChange = { cardPrefixText = it },
                            label = { Text("Prefix") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = cardStartText,
                            onValueChange = { cardStartText = it },
                            label = { Text("Start No.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = cardEndText,
                            onValueChange = { cardEndText = it },
                            label = { Text("End No.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SpaMist
                    ) {
                        Text(
                            text = "Sample Range: ${cardPrefixText}${cardStartText} to ${cardPrefixText}${cardEndText}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaDeepForest, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        // Steaming & Timing Intervals
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HotTub, contentDescription = null, tint = StatusSteamTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Steam Bath & Operational Buffer Timers",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaDarkGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = steamBetweenText,
                        onValueChange = { steamBetweenText = it },
                        label = { Text("Extra Steaming Minutes Between Treatments") },
                        supportingText = { Text("Standard user requirement: extra 15 mins for steaming on time") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = defaultPreSteamText,
                        onValueChange = { defaultPreSteamText = it },
                        label = { Text("Pre-Session Steam Bath Duration (mins)") },
                        supportingText = { Text("Time client spends in steam lounge before room is ready") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cleaningBufferText,
                        onValueChange = { cleaningBufferText = it },
                        label = { Text("Room Turnover / Cleaning Buffer (mins)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Suggest Next Therapist In Rotation", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Picks therapist with highest turn priority", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                        Switch(checked = autoSuggestNext, onCheckedChange = { autoSuggestNext = it })
                    }
                }
            }
        }

        // Offline System Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5E1A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusAvailableGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Offline SQLite Database Active",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF33691E))
                        )
                        Text(
                            text = "Full offline-first operations. All rooms, timers, queues, and settings are saved locally and persist across reboots.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF558B2F))
                        )
                    }
                }
            }
        }

        // Save Settings Action Button
        item {
            Button(
                onClick = {
                    val updated = activeSettings.copy(
                        cardStartNumber = cardStartText.toIntOrNull() ?: 101,
                        cardEndNumber = cardEndText.toIntOrNull() ?: 200,
                        cardPrefix = cardPrefixText.trim(),
                        steamBetweenExtraMinutes = steamBetweenText.toIntOrNull() ?: 15,
                        defaultPreSteamMinutes = defaultPreSteamText.toIntOrNull() ?: 15,
                        roomCleaningBufferMinutes = cleaningBufferText.toIntOrNull() ?: 5,
                        autoSuggestNextTherapist = autoSuggestNext
                    )
                    onSaveSettings(updated)
                    saveFeedback = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SpaDeepForest),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (saveFeedback) Icons.Default.Check else Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (saveFeedback) "Settings Saved!" else "Save Settings")
            }
        }
    }
}
