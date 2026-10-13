package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BookingStatus
import com.example.data.model.RoomType
import com.example.data.model.SessionStatus
import com.example.ui.components.AddRoomDialog
import com.example.ui.components.AddTherapistDialog
import com.example.ui.components.AddTreatmentDialog
import com.example.ui.components.BranchDialog
import com.example.ui.components.CheckInDialog
import com.example.ui.components.NewBookingDialog
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.FrontDeskScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TherapistQueueScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaDarkGreen
import com.example.ui.theme.SpaDeepForest
import com.example.ui.theme.SpaGoldDark
import com.example.ui.theme.SpaMist
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.viewmodel.SpaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SpaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaApp(viewModel: SpaViewModel = viewModel()) {
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val therapists by viewModel.therapists.collectAsStateWithLifecycle()
    val treatments by viewModel.treatments.collectAsStateWithLifecycle()
    val activeSessions by viewModel.activeSessions.collectAsStateWithLifecycle()
    val scheduledBookings by viewModel.scheduledBookings.collectAsStateWithLifecycle()
    val settings by viewModel.branchSettings.collectAsStateWithLifecycle()
    val currentTimeEpoch by viewModel.currentTimeEpoch.collectAsStateWithLifecycle()
    val waitTimeEstimate by viewModel.waitTimeEstimate.collectAsStateWithLifecycle()

    val showCheckInDialog by viewModel.showCheckInDialog.collectAsStateWithLifecycle()
    val preselectedRoom by viewModel.preselectedRoomForCheckIn.collectAsStateWithLifecycle()
    val suggestedCardNumber by viewModel.suggestedCardNumber.collectAsStateWithLifecycle()
    val showNewBookingDialog by viewModel.showNewBookingDialog.collectAsStateWithLifecycle()
    val showAddTherapistDialog by viewModel.showAddTherapistDialog.collectAsStateWithLifecycle()
    val showAddRoomDialog by viewModel.showAddRoomDialog.collectAsStateWithLifecycle()
    val showAddTreatmentDialog by viewModel.showAddTreatmentDialog.collectAsStateWithLifecycle()
    val showBranchDialog by viewModel.showBranchDialog.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }

    val currentBranch = remember(branches, selectedBranchId) {
        branches.find { it.id == selectedBranchId } ?: branches.firstOrNull()
    }

    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedCurrentTime = timeFormatter.format(Date(currentTimeEpoch))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Spa Emblem
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpaGoldDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Spa,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "SpaFlow Desk",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpaDarkGreen
                                )
                            )

                            // Branch Selector Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.openBranchDialog() }
                                    .testTag("branch_selector_trigger")
                            ) {
                                Text(
                                    text = currentBranch?.name ?: "Select Branch",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SpaDeepForest,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Switch Branch",
                                    tint = SpaDeepForest,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Live Time & Offline Ready Indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SpaMist,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = StatusAvailableGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedCurrentTime,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpaDarkGreen
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = SpaDarkGreen
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = SpaDeepForest,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeSessions.isNotEmpty()) {
                                    Badge(containerColor = SpaDeepForest) {
                                        Text("${activeSessions.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = "Front Desk")
                        }
                    },
                    label = { Text("Front Desk", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaDeepForest,
                        selectedTextColor = SpaDeepForest,
                        indicatorColor = SpaMist
                    ),
                    modifier = Modifier.testTag("nav_tab_front_desk")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                val availableCount = therapists.count { it.status == "AVAILABLE" }
                                if (availableCount > 0) {
                                    Badge(containerColor = SpaGoldDark) {
                                        Text("$availableCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Therapists")
                        }
                    },
                    label = { Text("Therapists", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaDeepForest,
                        selectedTextColor = SpaDeepForest,
                        indicatorColor = SpaMist
                    ),
                    modifier = Modifier.testTag("nav_tab_therapists")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (scheduledBookings.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFFE65100)) {
                                        Text("${scheduledBookings.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Bookings")
                        }
                    },
                    label = { Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaDeepForest,
                        selectedTextColor = SpaDeepForest,
                        indicatorColor = SpaMist
                    ),
                    modifier = Modifier.testTag("nav_tab_bookings")
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Spa, contentDescription = "Catalog") },
                    label = { Text("Catalog", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaDeepForest,
                        selectedTextColor = SpaDeepForest,
                        indicatorColor = SpaMist
                    ),
                    modifier = Modifier.testTag("nav_tab_catalog")
                )

                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaDeepForest,
                        selectedTextColor = SpaDeepForest,
                        indicatorColor = SpaMist
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> FrontDeskScreen(
                    rooms = rooms,
                    activeSessions = activeSessions,
                    currentTimeEpoch = currentTimeEpoch,
                    waitTimeEstimate = waitTimeEstimate,
                    onOpenCheckIn = { room -> viewModel.openCheckInDialog(room) },
                    onStartTreatment = { id -> viewModel.startTreatment(id) },
                    onCompleteTreatment = { id -> viewModel.completeTreatment(id) },
                    onAddOvertime = { id, mins -> viewModel.addOvertime(id, mins) },
                    onTransitionPreSteam = { id -> viewModel.transitionPreSteamToRoom(id) }
                )
                1 -> TherapistQueueScreen(
                    therapists = therapists,
                    onMoveUp = { id -> viewModel.moveTherapistUp(id) },
                    onMoveDown = { id -> viewModel.moveTherapistDown(id) },
                    onStatusToggle = { id, status -> viewModel.toggleTherapistStatus(id, status) },
                    onOpenAddTherapist = { viewModel.openAddTherapistDialog() }
                )
                2 -> BookingsScreen(
                    bookings = scheduledBookings,
                    onOpenNewBooking = { viewModel.openNewBookingDialog() },
                    onCancelBooking = { booking -> viewModel.cancelBooking(booking) },
                    onCheckInBooking = { booking ->
                        viewModel.openCheckInDialog()
                    }
                )
                3 -> CatalogScreen(
                    treatments = treatments,
                    rooms = rooms,
                    onOpenAddTreatment = { viewModel.openAddTreatmentDialog() },
                    onOpenAddRoom = { viewModel.openAddRoomDialog() }
                )
                4 -> SettingsScreen(
                    currentBranch = currentBranch,
                    branches = branches,
                    settings = settings,
                    onOpenBranchDialog = { viewModel.openBranchDialog() },
                    onSaveSettings = { updated -> viewModel.updateBranchSettings(updated) }
                )
            }
        }
    }

    // Modal Dialogs
    CheckInDialog(
        isOpen = showCheckInDialog,
        suggestedCardNumber = suggestedCardNumber,
        preselectedRoom = preselectedRoom,
        rooms = rooms,
        therapists = therapists,
        treatments = treatments,
        settings = settings,
        onDismiss = { viewModel.closeCheckInDialog() },
        onConfirmCheckIn = { name, phone, isCouple, secName, selectedTreatments, therapist, secTherapist, room, hasSteam, hasPreSteam, cardNum, notes ->
            viewModel.checkInClient(
                clientName = name,
                clientPhone = phone,
                isCouple = isCouple,
                secondaryClientName = secName,
                selectedTreatments = selectedTreatments,
                therapist = therapist,
                secondaryTherapist = secTherapist,
                room = room,
                hasSteamBetween = hasSteam,
                hasPreSteam = hasPreSteam,
                customCardNumber = cardNum,
                notes = notes
            )
        }
    )

    NewBookingDialog(
        isOpen = showNewBookingDialog,
        therapists = therapists,
        onDismiss = { viewModel.closeNewBookingDialog() },
        onConfirmBooking = { booking -> viewModel.addBooking(booking) }
    )

    AddTherapistDialog(
        isOpen = showAddTherapistDialog,
        onDismiss = { viewModel.closeAddTherapistDialog() },
        onConfirm = { therapist -> viewModel.addTherapist(therapist) }
    )

    AddRoomDialog(
        isOpen = showAddRoomDialog,
        onDismiss = { viewModel.closeAddRoomDialog() },
        onConfirm = { room -> viewModel.addRoom(room) }
    )

    AddTreatmentDialog(
        isOpen = showAddTreatmentDialog,
        onDismiss = { viewModel.closeAddTreatmentDialog() },
        onConfirm = { treatment -> viewModel.addTreatment(treatment) }
    )

    BranchDialog(
        isOpen = showBranchDialog,
        branches = branches,
        currentBranchId = selectedBranchId,
        onSelectBranch = { branchId -> viewModel.selectBranch(branchId) },
        onAddBranch = { branch -> viewModel.addBranch(branch) },
        onDismiss = { viewModel.closeBranchDialog() }
    )
}
