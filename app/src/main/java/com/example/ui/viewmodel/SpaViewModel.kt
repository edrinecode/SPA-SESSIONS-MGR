package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SpaDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.BranchEntity
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.ClientSessionEntity
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.data.model.TreatmentEntity
import com.example.data.repository.SpaRepository
import com.example.data.repository.WaitTimeEstimate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpaRepository

    init {
        val database = SpaDatabase.getDatabase(application)
        repository = SpaRepository(database.spaDao())
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    private val _selectedBranchId = MutableStateFlow("branch_main")
    val selectedBranchId: StateFlow<String> = _selectedBranchId.asStateFlow()

    val branches: StateFlow<List<BranchEntity>> = repository.getAllBranches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rooms: StateFlow<List<SpaRoomEntity>> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getRooms(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val therapists: StateFlow<List<TherapistEntity>> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getTherapists(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treatments: StateFlow<List<TreatmentEntity>> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getTreatments(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSessions: StateFlow<List<ClientSessionEntity>> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getActiveSessions(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSessions: StateFlow<List<ClientSessionEntity>> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getRecentSessions(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledBookings: StateFlow<List<BookingEntity>> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getScheduledBookings(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val branchSettings: StateFlow<BranchSettingsEntity?> = _selectedBranchId
        .flatMapLatest { branchId -> repository.getBranchSettings(branchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Real-time ticking clock for timers, progress bars, and wait-time estimations (updates every second)
    val currentTimeEpoch: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    private val _waitTimeEstimate = MutableStateFlow(
        WaitTimeEstimate(
            areAllRoomsBusy = false,
            totalRooms = 0,
            freeRoomsCount = 0,
            inUseRoomsCount = 0,
            earliestRoomFreeName = null,
            earliestRoomTherapistName = null,
            minutesUntilEarliestRoomFree = 0,
            nextTherapistInTurn = null,
            steamLoungeCapacityAvailable = true,
            adviceMessage = "Loading status..."
        )
    )
    val waitTimeEstimate: StateFlow<WaitTimeEstimate> = _waitTimeEstimate.asStateFlow()

    // Dialog & Navigation UI states
    private val _showCheckInDialog = MutableStateFlow(false)
    val showCheckInDialog = _showCheckInDialog.asStateFlow()

    private val _preselectedRoomForCheckIn = MutableStateFlow<SpaRoomEntity?>(null)
    val preselectedRoomForCheckIn = _preselectedRoomForCheckIn.asStateFlow()

    private val _showNewBookingDialog = MutableStateFlow(false)
    val showNewBookingDialog = _showNewBookingDialog.asStateFlow()

    private val _showAddTherapistDialog = MutableStateFlow(false)
    val showAddTherapistDialog = _showAddTherapistDialog.asStateFlow()

    private val _showAddRoomDialog = MutableStateFlow(false)
    val showAddRoomDialog = _showAddRoomDialog.asStateFlow()

    private val _showAddTreatmentDialog = MutableStateFlow(false)
    val showAddTreatmentDialog = _showAddTreatmentDialog.asStateFlow()

    private val _showBranchDialog = MutableStateFlow(false)
    val showBranchDialog = _showBranchDialog.asStateFlow()

    private val _suggestedCardNumber = MutableStateFlow("")
    val suggestedCardNumber = _suggestedCardNumber.asStateFlow()

    init {
        // Re-evaluate wait time whenever rooms, sessions, therapists, or branch change
        viewModelScope.launch {
            combine(rooms, activeSessions, therapists, _selectedBranchId) { _, _, _, branchId ->
                branchId
            }.collect { branchId ->
                refreshWaitTimeEstimate(branchId)
            }
        }
        // Expire room turnover locally; no server or network connection is required.
        viewModelScope.launch {
            combine(currentTimeEpoch, _selectedBranchId) { now, branchId -> now to branchId }
                .collect { (now, branchId) -> repository.releaseRoomsAfterCleaning(branchId, now) }
        }
    }

    private suspend fun refreshWaitTimeEstimate(branchId: String) {
        val estimate = repository.calculateWaitTimeInfo(branchId)
        _waitTimeEstimate.value = estimate
    }

    fun selectBranch(branchId: String) {
        _selectedBranchId.value = branchId
    }

    fun openCheckInDialog(targetRoom: SpaRoomEntity? = null) {
        _preselectedRoomForCheckIn.value = targetRoom
        viewModelScope.launch {
            _suggestedCardNumber.value = repository.getNextFreeCardNumber(_selectedBranchId.value)
            _showCheckInDialog.value = true
        }
    }

    fun closeCheckInDialog() {
        _showCheckInDialog.value = false
        _preselectedRoomForCheckIn.value = null
    }

    fun openNewBookingDialog() {
        _showNewBookingDialog.value = true
    }

    fun closeNewBookingDialog() {
        _showNewBookingDialog.value = false
    }

    fun openAddTherapistDialog() {
        _showAddTherapistDialog.value = true
    }

    fun closeAddTherapistDialog() {
        _showAddTherapistDialog.value = false
    }

    fun openAddRoomDialog() {
        _showAddRoomDialog.value = true
    }

    fun closeAddRoomDialog() {
        _showAddRoomDialog.value = false
    }

    fun openAddTreatmentDialog() {
        _showAddTreatmentDialog.value = true
    }

    fun closeAddTreatmentDialog() {
        _showAddTreatmentDialog.value = false
    }

    fun openBranchDialog() {
        _showBranchDialog.value = true
    }

    fun closeBranchDialog() {
        _showBranchDialog.value = false
    }

    fun checkInClient(
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
        customCardNumber: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.checkInClient(
                branchId = _selectedBranchId.value,
                clientName = clientName,
                clientPhone = clientPhone,
                isCouple = isCouple,
                secondaryClientName = secondaryClientName,
                selectedTreatments = selectedTreatments,
                therapist = therapist,
                secondaryTherapist = secondaryTherapist,
                room = room,
                hasSteamBetween = hasSteamBetween,
                hasPreSteam = hasPreSteam,
                customCardNumber = customCardNumber,
                notes = notes
            )
            closeCheckInDialog()
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    // Therapist calls front desk: starting treatment
    fun startTreatment(sessionId: String) {
        viewModelScope.launch {
            repository.startTreatment(sessionId)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    // Therapist calls front desk: treatment completed
    fun completeTreatment(sessionId: String) {
        viewModelScope.launch {
            repository.completeTreatment(sessionId)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    // Move client from pre-session steam bath to treatment room
    fun transitionPreSteamToRoom(sessionId: String) {
        viewModelScope.launch {
            repository.transitionPreSteamToRoom(sessionId)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    fun addOvertime(sessionId: String, minutes: Int) {
        viewModelScope.launch {
            repository.addOvertime(sessionId, minutes)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    fun moveTherapistUp(therapistId: String) {
        viewModelScope.launch {
            repository.moveTherapistUp(_selectedBranchId.value, therapistId)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    fun moveTherapistDown(therapistId: String) {
        viewModelScope.launch {
            repository.moveTherapistDown(_selectedBranchId.value, therapistId)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    fun toggleTherapistStatus(therapistId: String, newStatus: TherapistStatus) {
        viewModelScope.launch {
            repository.toggleTherapistStatus(therapistId, newStatus)
            refreshWaitTimeEstimate(_selectedBranchId.value)
        }
    }

    fun addBooking(booking: BookingEntity) {
        viewModelScope.launch {
            repository.addBooking(booking.copy(branchId = _selectedBranchId.value))
            closeNewBookingDialog()
        }
    }

    fun cancelBooking(booking: BookingEntity) {
        viewModelScope.launch {
            repository.cancelBooking(booking)
        }
    }

    fun addTherapist(therapist: TherapistEntity) {
        viewModelScope.launch {
            val maxOrder = therapists.value.maxOfOrNull { it.orderIndex } ?: 0
            repository.addTherapist(
                therapist.copy(
                    branchId = _selectedBranchId.value,
                    orderIndex = maxOrder + 1
                )
            )
            closeAddTherapistDialog()
        }
    }

    fun addRoom(room: SpaRoomEntity) {
        viewModelScope.launch {
            repository.addRoom(room.copy(branchId = _selectedBranchId.value))
            closeAddRoomDialog()
        }
    }

    fun addTreatment(treatment: TreatmentEntity) {
        viewModelScope.launch {
            repository.addTreatment(treatment.copy(branchId = _selectedBranchId.value))
            closeAddTreatmentDialog()
        }
    }

    fun updateBranchSettings(settings: BranchSettingsEntity) {
        viewModelScope.launch {
            repository.updateBranchSettings(settings)
        }
    }

    fun addBranch(branch: BranchEntity) {
        viewModelScope.launch {
            repository.addBranch(branch)
            _selectedBranchId.value = branch.id
            closeBranchDialog()
        }
    }
}
