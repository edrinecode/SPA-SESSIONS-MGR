package com.example.data.repository

import com.example.data.local.SpaDao
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.BranchEntity
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.ClientSessionEntity
import com.example.data.model.RoomStatus
import com.example.data.model.RoomType
import com.example.data.model.SessionStatus
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.data.model.TreatmentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

data class QueuedClientWaitEstimate(
    val sessionId: String,
    val queuePosition: Int,
    val estimatedWaitMinutes: Int,
    val targetRoomName: String,
    val assignedTherapistName: String,
    val adviceMessage: String
)

data class WaitTimeEstimate(
    val areAllRoomsBusy: Boolean,
    val totalRooms: Int,
    val freeRoomsCount: Int,
    val inUseRoomsCount: Int,
    val waitingClientsCount: Int = 0,
    val earliestRoomFreeName: String?,
    val earliestRoomTherapistName: String?,
    val minutesUntilEarliestRoomFree: Int,
    val nextWalkInWaitMinutes: Int = 0,
    val nextTherapistInTurn: TherapistEntity?,
    val steamLoungeCapacityAvailable: Boolean,
    val adviceMessage: String,
    val clientWaitEstimates: Map<String, QueuedClientWaitEstimate> = emptyMap()
)

class SpaRepository(private val dao: SpaDao) {

    fun getAllBranches(): Flow<List<BranchEntity>> = dao.getAllBranches()

    fun getRooms(branchId: String): Flow<List<SpaRoomEntity>> = dao.getRoomsForBranch(branchId)

    fun getTherapists(branchId: String): Flow<List<TherapistEntity>> = dao.getTherapistsForBranch(branchId)

    fun getTreatments(branchId: String): Flow<List<TreatmentEntity>> = dao.getTreatmentsForBranch(branchId)

    fun getActiveSessions(branchId: String): Flow<List<ClientSessionEntity>> = dao.getActiveSessionsForBranch(branchId)

    fun getRecentSessions(branchId: String): Flow<List<ClientSessionEntity>> = dao.getRecentSessionsForBranch(branchId)

    fun getScheduledBookings(branchId: String): Flow<List<BookingEntity>> = dao.getScheduledBookingsForBranch(branchId)

    fun getAllBookings(branchId: String): Flow<List<BookingEntity>> = dao.getAllBookingsForBranch(branchId)

    fun getBranchSettings(branchId: String): Flow<BranchSettingsEntity?> = dao.getSettingsForBranch(branchId)

    suspend fun ensureDefaultData() {
        val branches = dao.getAllBranches().first()
        val branchId = if (branches.isEmpty()) {
            val defaultId = "branch_main"
            dao.insertBranch(
                BranchEntity(
                    id = defaultId,
                    name = "Positive Emotions Fitness and Spa",
                    isDefault = true
                )
            )
            dao.insertOrUpdateSettings(BranchSettingsEntity(branchId = defaultId))
            defaultId
        } else {
            branches.first().id
        }

        // Populate rooms if branch has none
        val existingRooms = dao.getRoomsForBranch(branchId).first()
        if (existingRooms.isEmpty()) {
            dao.insertRooms(
                listOf(
                    SpaRoomEntity("room_1", branchId, "Room 1 (Single Suite)", RoomType.SINGLE.name, capacity = 1),
                    SpaRoomEntity("room_2", branchId, "Room 2 (Single Suite)", RoomType.SINGLE.name, capacity = 1),
                    SpaRoomEntity("room_3", branchId, "Room 3 (Couple Suite)", RoomType.COUPLE.name, capacity = 2),
                    SpaRoomEntity("room_4", branchId, "Room 4 (Couple Suite)", RoomType.COUPLE.name, capacity = 2),
                    SpaRoomEntity("room_steam", branchId, "Thermal Steam Lounge", RoomType.STEAM_BATH.name, capacity = 6)
                )
            )
        }

        // Populate therapists if branch has none (in manager rotation order)
        val existingTherapists = dao.getTherapistsForBranch(branchId).first()
        if (existingTherapists.isEmpty()) {
            dao.insertTherapists(
                listOf(
                    TherapistEntity("th_1", branchId, "Maya Lin", specialties = "Swedish, Deep Tissue", orderIndex = 1),
                    TherapistEntity("th_2", branchId, "David Vance", specialties = "Sports, Reflexology", orderIndex = 2),
                    TherapistEntity("th_3", branchId, "Sarah Jenkins", specialties = "Aromatherapy, Hot Stone", orderIndex = 3),
                    TherapistEntity("th_4", branchId, "Elena Cruz", specialties = "Couple Massage, Body Scrub", orderIndex = 4),
                    TherapistEntity("th_5", branchId, "John Kamau", specialties = "Deep Tissue, Steam Therapy", orderIndex = 5)
                )
            )
        }

        // Populate treatments if branch has none
        val existingTreatments = dao.getTreatmentsForBranch(branchId).first()
        if (existingTreatments.isEmpty()) {
            dao.insertTreatments(
                listOf(
                    TreatmentEntity("tr_1", branchId, "Swedish Relaxing Massage", category = "Massage", durationMinutes = 60, price = 50.0),
                    TreatmentEntity("tr_2", branchId, "Deep Tissue Therapy", category = "Massage", durationMinutes = 75, price = 65.0, hasSteamBetween = true),
                    TreatmentEntity("tr_3", branchId, "Aromatherapy Wellness", category = "Massage", durationMinutes = 60, price = 55.0),
                    TreatmentEntity("tr_4", branchId, "Hot Stone Deluxe", category = "Therapy", durationMinutes = 90, price = 80.0, hasSteamBetween = true),
                    TreatmentEntity("tr_5", branchId, "Couple Harmony Massage", category = "Couple", durationMinutes = 90, price = 120.0, hasSteamBetween = true),
                    TreatmentEntity("tr_6", branchId, "Body Scrub & Steam Session", category = "Body Care", durationMinutes = 60, price = 50.0, hasSteamBetween = true),
                    TreatmentEntity("tr_7", branchId, "Express Back & Neck Relief", category = "Express", durationMinutes = 30, price = 30.0),
                    TreatmentEntity("tr_8", branchId, "Thermal Steam Bath", category = "Steam Bath", durationMinutes = 30, price = 25.0)
                )
            )
        }
    }

    suspend fun getNextFreeCardNumber(branchId: String): String {
        val settings = dao.getSettingsSync(branchId) ?: BranchSettingsEntity(branchId)
        val activeSessions = dao.getActiveSessionsForBranch(branchId).first()
        val usedCardNumbers = activeSessions.map { it.cardNumber }.toSet()

        val prefix = settings.cardPrefix
        for (num in settings.cardStartNumber..settings.cardEndNumber) {
            val candidate = "$prefix$num"
            if (!usedCardNumbers.contains(candidate)) {
                return candidate
            }
        }
        return "$prefix${settings.cardStartNumber + activeSessions.size}"
    }

    suspend fun checkInClient(
        branchId: String,
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
    ): ClientSessionEntity {
        val settings = dao.getSettingsSync(branchId) ?: BranchSettingsEntity(branchId)
        val cardNumber = customCardNumber?.ifBlank { null } ?: getNextFreeCardNumber(branchId)

        val baseTreatmentMinutes = selectedTreatments.sumOf { it.durationMinutes }
        val extraSteamMinutes = if (hasSteamBetween) settings.steamBetweenExtraMinutes else 0
        val totalMinutes = baseTreatmentMinutes + extraSteamMinutes

        val treatmentNamesFormatted = selectedTreatments.joinToString(" + ") { "${it.name} (${it.durationMinutes}m)" } +
                (if (hasSteamBetween) " + Steam Bath (${settings.steamBetweenExtraMinutes}m)" else "")

        val sessionId = "sess_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val isTargetRoomFree = room.status == RoomStatus.FREE.name &&
                (room.cleaningUntilEpoch ?: 0L) <= now &&
                room.currentSessionId == null

        val initialStatus = when {
            hasPreSteam -> SessionStatus.IN_PRE_STEAM.name
            isTargetRoomFree -> SessionStatus.READY_FOR_ROOM.name
            else -> SessionStatus.WAITING.name
        }

        val session = ClientSessionEntity(
            id = sessionId,
            branchId = branchId,
            cardNumber = cardNumber,
            clientName = clientName.trim(),
            clientPhone = clientPhone.trim(),
            isCouple = isCouple,
            secondaryClientName = secondaryClientName?.trim()?.ifBlank { null },
            treatmentIds = selectedTreatments.joinToString(",") { it.id },
            treatmentNames = treatmentNamesFormatted,
            therapistId = therapist.id,
            therapistName = therapist.name,
            secondaryTherapistId = secondaryTherapist?.id,
            secondaryTherapistName = secondaryTherapist?.name,
            roomId = room.id,
            roomName = room.name,
            hasSteamBetween = hasSteamBetween,
            hasPreSteam = hasPreSteam,
            preSteamDurationMinutes = settings.defaultPreSteamMinutes,
            preSteamStartedAt = if (hasPreSteam) now else null,
            totalDurationMinutes = totalMinutes,
            checkInTime = now,
            startedAt = null,
            status = initialStatus,
            notes = notes
        )

        dao.insertSession(session)

        // Room and therapist updates depending on availability:
        if (isTargetRoomFree && !hasPreSteam) {
            dao.updateRoom(
                room.copy(
                    status = RoomStatus.IN_USE.name,
                    currentSessionId = sessionId
                )
            )
            dao.updateTherapist(
                therapist.copy(
                    status = TherapistStatus.IN_ROOM.name,
                    activeSessionId = sessionId,
                    currentRoomName = room.name
                )
            )
            if (secondaryTherapist != null) {
                dao.updateTherapist(
                    secondaryTherapist.copy(
                        status = TherapistStatus.IN_ROOM.name,
                        activeSessionId = sessionId,
                        currentRoomName = room.name
                    )
                )
            }
        } else if (hasPreSteam) {
            if (isTargetRoomFree) {
                dao.updateRoom(
                    room.copy(
                        status = RoomStatus.RESERVED.name,
                        currentSessionId = sessionId
                    )
                )
            }
            dao.updateTherapist(
                therapist.copy(
                    status = TherapistStatus.BOOKED.name,
                    activeSessionId = sessionId,
                    currentRoomName = "Steam Lounge"
                )
            )
            if (secondaryTherapist != null) {
                dao.updateTherapist(
                    secondaryTherapist.copy(
                        status = TherapistStatus.BOOKED.name,
                        activeSessionId = sessionId,
                        currentRoomName = "Steam Lounge"
                    )
                )
            }
        } else {
            // Room is occupied: client waits in queue
            dao.updateTherapist(
                therapist.copy(
                    status = if (therapist.status == TherapistStatus.AVAILABLE.name) TherapistStatus.BOOKED.name else therapist.status,
                    activeSessionId = sessionId
                )
            )
        }

        return session
    }

    suspend fun seatWaitingClientInRoom(sessionId: String, targetRoomId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        val targetRoom = dao.getRoomById(targetRoomId) ?: return
        val therapist = dao.getTherapistById(session.therapistId)

        val updatedSession = session.copy(
            roomId = targetRoom.id,
            roomName = targetRoom.name,
            status = SessionStatus.READY_FOR_ROOM.name
        )
        dao.updateSession(updatedSession)

        dao.updateRoom(
            targetRoom.copy(
                status = RoomStatus.IN_USE.name,
                currentSessionId = sessionId,
                cleaningUntilEpoch = null
            )
        )

        if (therapist != null) {
            dao.updateTherapist(
                therapist.copy(
                    status = TherapistStatus.IN_ROOM.name,
                    activeSessionId = sessionId,
                    currentRoomName = targetRoom.name
                )
            )
        }
    }

    suspend fun sendWaitingClientToSteam(sessionId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            status = SessionStatus.IN_PRE_STEAM.name,
            preSteamStartedAt = System.currentTimeMillis()
        )
        dao.updateSession(updatedSession)
    }

    suspend fun cancelWaitingSession(sessionId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        dao.updateSession(session.copy(status = SessionStatus.CANCELLED.name))

        val room = dao.getRoomById(session.roomId)
        if (room != null && room.currentSessionId == sessionId) {
            dao.updateRoom(room.copy(status = RoomStatus.FREE.name, currentSessionId = null))
        }

        val therapist = dao.getTherapistById(session.therapistId)
        if (therapist != null && therapist.activeSessionId == sessionId) {
            dao.updateTherapist(therapist.copy(status = TherapistStatus.AVAILABLE.name, activeSessionId = null, currentRoomName = null))
        }
    }

    suspend fun transitionPreSteamToRoom(sessionId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        val room = dao.getRoomById(session.roomId)
        val therapist = dao.getTherapistById(session.therapistId)

        val updatedSession = session.copy(
            status = SessionStatus.READY_FOR_ROOM.name,
            preSteamFinishedAt = System.currentTimeMillis()
        )
        dao.updateSession(updatedSession)

        if (room != null) {
            dao.updateRoom(room.copy(status = RoomStatus.IN_USE.name, currentSessionId = sessionId))
        }
        if (therapist != null) {
            dao.updateTherapist(
                therapist.copy(
                    status = TherapistStatus.IN_ROOM.name,
                    activeSessionId = sessionId,
                    currentRoomName = session.roomName
                )
            )
        }
    }

    suspend fun startTreatment(sessionId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        val now = System.currentTimeMillis()
        val updatedSession = session.copy(
            startedAt = now,
            status = SessionStatus.IN_TREATMENT.name
        )
        dao.updateSession(updatedSession)

        val room = dao.getRoomById(session.roomId)
        if (room != null) {
            dao.updateRoom(room.copy(status = RoomStatus.IN_USE.name, currentSessionId = sessionId))
        }

        val therapist = dao.getTherapistById(session.therapistId)
        if (therapist != null) {
            dao.updateTherapist(
                therapist.copy(
                    status = TherapistStatus.IN_ROOM.name,
                    activeSessionId = sessionId,
                    currentRoomName = session.roomName
                )
            )
        }
        if (session.secondaryTherapistId != null) {
            val secTherapist = dao.getTherapistById(session.secondaryTherapistId)
            if (secTherapist != null) {
                dao.updateTherapist(
                    secTherapist.copy(
                        status = TherapistStatus.IN_ROOM.name,
                        activeSessionId = sessionId,
                        currentRoomName = session.roomName
                    )
                )
            }
        }
    }

    suspend fun completeTreatment(sessionId: String) {
        val session = dao.getSessionById(sessionId) ?: return
        val branchId = session.branchId
        val now = System.currentTimeMillis()

        dao.updateSession(
            session.copy(
                completedAt = now,
                status = SessionStatus.COMPLETED.name
            )
        )

        // Free room with cleaning buffer
        val room = dao.getRoomById(session.roomId)
        if (room != null) {
            val settings = dao.getSettingsSync(branchId) ?: BranchSettingsEntity(branchId)
            val bufferMs = settings.roomCleaningBufferMinutes * 60 * 1000L
            dao.updateRoom(
                room.copy(
                    status = if (bufferMs > 0L) RoomStatus.CLEANING.name else RoomStatus.FREE.name,
                    currentSessionId = null,
                    cleaningUntilEpoch = if (bufferMs > 0L) now + bufferMs else null
                )
            )
        }

        // Return therapist to queue rotation at bottom
        rotateTherapistToQueueEnd(branchId, session.therapistId)
        if (session.secondaryTherapistId != null) {
            rotateTherapistToQueueEnd(branchId, session.secondaryTherapistId)
        }
    }

    private suspend fun rotateTherapistToQueueEnd(branchId: String, therapistId: String) {
        val therapists = dao.getTherapistsForBranch(branchId).first()
        val current = therapists.find { it.id == therapistId } ?: return
        val maxOrder = therapists.maxOfOrNull { it.orderIndex } ?: 0

        dao.updateTherapist(
            current.copy(
                orderIndex = maxOrder + 1,
                status = TherapistStatus.AVAILABLE.name,
                activeSessionId = null,
                currentRoomName = null,
                completedSessionsToday = current.completedSessionsToday + 1
            )
        )
    }

    suspend fun addOvertime(sessionId: String, minutes: Int) {
        val session = dao.getSessionById(sessionId) ?: return
        dao.updateSession(
            session.copy(
                addedOvertimeMinutes = session.addedOvertimeMinutes + minutes
            )
        )
    }

    suspend fun moveTherapistUp(branchId: String, therapistId: String) {
        val therapists = dao.getTherapistsForBranch(branchId).first().sortedBy { it.orderIndex }.toMutableList()
        val index = therapists.indexOfFirst { it.id == therapistId }
        if (index > 0) {
            val current = therapists[index]
            val previous = therapists[index - 1]

            val currentOrder = current.orderIndex
            val prevOrder = previous.orderIndex

            dao.updateTherapist(current.copy(orderIndex = prevOrder))
            dao.updateTherapist(previous.copy(orderIndex = currentOrder))
        }
    }

    suspend fun moveTherapistDown(branchId: String, therapistId: String) {
        val therapists = dao.getTherapistsForBranch(branchId).first().sortedBy { it.orderIndex }.toMutableList()
        val index = therapists.indexOfFirst { it.id == therapistId }
        if (index >= 0 && index < therapists.size - 1) {
            val current = therapists[index]
            val next = therapists[index + 1]

            val currentOrder = current.orderIndex
            val nextOrder = next.orderIndex

            dao.updateTherapist(current.copy(orderIndex = nextOrder))
            dao.updateTherapist(next.copy(orderIndex = currentOrder))
        }
    }

    suspend fun toggleTherapistStatus(therapistId: String, newStatus: TherapistStatus) {
        val therapist = dao.getTherapistById(therapistId) ?: return
        dao.updateTherapist(therapist.copy(status = newStatus.name))
    }

    suspend fun addTherapist(therapist: TherapistEntity) {
        dao.insertTherapist(therapist)
    }

    suspend fun updateTherapist(therapist: TherapistEntity) {
        dao.updateTherapist(therapist)
    }

    suspend fun deleteTherapist(therapist: TherapistEntity) {
        dao.deleteTherapist(therapist)
    }

    suspend fun addRoom(room: SpaRoomEntity) {
        dao.insertRoom(room)
    }

    suspend fun updateRoom(room: SpaRoomEntity) {
        dao.updateRoom(room)
    }

    suspend fun deleteRoom(room: SpaRoomEntity) {
        dao.deleteRoom(room)
    }

    suspend fun addTreatment(treatment: TreatmentEntity) {
        dao.insertTreatment(treatment)
    }

    suspend fun updateTreatment(treatment: TreatmentEntity) {
        dao.updateTreatment(treatment)
    }

    suspend fun deleteTreatment(treatment: TreatmentEntity) {
        dao.deleteTreatment(treatment)
    }

    suspend fun addBooking(booking: BookingEntity) {
        dao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: BookingEntity) {
        dao.updateBooking(booking)
    }

    suspend fun cancelBooking(booking: BookingEntity) {
        dao.updateBooking(booking.copy(status = BookingStatus.CANCELLED.name))
    }

    suspend fun updateBranchSettings(settings: BranchSettingsEntity) {
        dao.insertOrUpdateSettings(settings)
    }

    suspend fun releaseRoomsAfterCleaning(branchId: String, now: Long = System.currentTimeMillis()) {
        dao.getRoomsForBranch(branchId).first()
            .filter { it.status == RoomStatus.CLEANING.name && (it.cleaningUntilEpoch ?: 0L) <= now }
            .forEach { room ->
                dao.updateRoom(room.copy(status = RoomStatus.FREE.name, cleaningUntilEpoch = null))
            }
    }

    suspend fun addBranch(branch: BranchEntity) {
        dao.insertBranch(branch)
        dao.insertOrUpdateSettings(BranchSettingsEntity(branchId = branch.id))
    }

    // Wait Time & Smart Receptionist Multi-Client Simulation Calculation
    suspend fun calculateWaitTimeInfo(branchId: String): WaitTimeEstimate {
        val allBranchRooms = dao.getRoomsForBranch(branchId).first()
        val rooms = allBranchRooms.filter { it.roomType != RoomType.STEAM_BATH.name }
        val activeSessions = dao.getActiveSessionsForBranch(branchId).first()
        val therapists = dao.getTherapistsForBranch(branchId).first()

        val now = System.currentTimeMillis()
        val settings = dao.getSettingsSync(branchId) ?: BranchSettingsEntity(branchId)
        val turnoverMs = settings.roomCleaningBufferMinutes.coerceAtLeast(0) * 60 * 1000L

        val freeRooms = rooms.filter {
            it.status == RoomStatus.FREE.name && (it.cleaningUntilEpoch ?: 0L) <= now
        }
        val areAllRoomsBusy = freeRooms.isEmpty() && rooms.isNotEmpty()

        val nextAvailableTherapist = therapists
            .filter { it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday }
            .minByOrNull { it.orderIndex }

        // Track when each room will be free
        val roomAvailabilityMap = mutableMapOf<String, Long>()
        for (room in rooms) {
            when (room.status) {
                RoomStatus.FREE.name -> {
                    roomAvailabilityMap[room.id] = now
                }
                RoomStatus.CLEANING.name -> {
                    val remainingMs = ((room.cleaningUntilEpoch ?: now) - now).coerceAtLeast(0L)
                    roomAvailabilityMap[room.id] = now + remainingMs
                }
                RoomStatus.IN_USE.name -> {
                    val inTreatmentSession = activeSessions.find {
                        it.roomId == room.id && it.status == SessionStatus.IN_TREATMENT.name
                    }
                    if (inTreatmentSession != null) {
                        val startTime = inTreatmentSession.startedAt ?: inTreatmentSession.checkInTime
                        val totalAllowedMs = (inTreatmentSession.totalDurationMinutes + inTreatmentSession.addedOvertimeMinutes) * 60 * 1000L
                        val elapsedMs = now - startTime
                        val remainingMs = (totalAllowedMs - elapsedMs).coerceAtLeast(0L)
                        roomAvailabilityMap[room.id] = now + remainingMs + turnoverMs
                    } else {
                        // In room but not started yet (e.g. READY_FOR_ROOM)
                        val readySession = activeSessions.find { it.roomId == room.id }
                        val expectedDur = ((readySession?.totalDurationMinutes ?: 60) * 60 * 1000L)
                        roomAvailabilityMap[room.id] = now + expectedDur + turnoverMs
                    }
                }
                RoomStatus.RESERVED.name -> {
                    roomAvailabilityMap[room.id] = now + (15 * 60 * 1000L)
                }
                else -> {
                    roomAvailabilityMap[room.id] = now
                }
            }
        }

        // Multi-client queue simulation:
        // Collect all clients waiting in lounge or pre-steam
        val waitingSessions = activeSessions
            .filter { it.status == SessionStatus.WAITING.name || it.status == SessionStatus.IN_PRE_STEAM.name }
            .sortedBy { it.checkInTime }

        val simulatedTimeline = roomAvailabilityMap.toMutableMap()
        val clientWaitEstimates = mutableMapOf<String, QueuedClientWaitEstimate>()

        var earliestRoomName: String? = null
        var earliestTherapistName: String? = null
        var earliestWaitMinutes = 0

        // Find immediately earliest room freeing
        val earliestEntry = simulatedTimeline.minByOrNull { it.value }
        if (earliestEntry != null && rooms.isNotEmpty()) {
            val earliestRoom = rooms.find { it.id == earliestEntry.key }
            earliestRoomName = earliestRoom?.name
            val earliestSession = activeSessions.find { it.roomId == earliestEntry.key }
            earliestTherapistName = earliestSession?.therapistName
            val waitMs = (earliestEntry.value - now).coerceAtLeast(0L)
            earliestWaitMinutes = ((waitMs + 59_999L) / 60_000L).toInt()
        }

        // Calculate individual wait time for each waiting client in FIFO order
        waitingSessions.forEachIndexed { index, session ->
            val queuePos = index + 1
            // Match room type if couple
            val candidateRooms = rooms.filter { r ->
                if (session.isCouple) r.roomType == RoomType.COUPLE.name else true
            }.ifEmpty { rooms }

            val bestRoom = candidateRooms.minByOrNull { simulatedTimeline[it.id] ?: now } ?: rooms.first()
            val availableAt = simulatedTimeline[bestRoom.id] ?: now
            val waitMs = (availableAt - now).coerceAtLeast(0L)
            val waitMins = if (waitMs <= 0L) 0 else ((waitMs + 59_999L) / 60_000L).toInt()
            val isReady = waitMins == 0

            val advice = if (isReady) {
                "${bestRoom.name} is ready now!"
            } else {
                "Wait ~$waitMins min • ${bestRoom.name} frees next"
            }

            clientWaitEstimates[session.id] = QueuedClientWaitEstimate(
                sessionId = session.id,
                queuePosition = queuePos,
                estimatedWaitMinutes = waitMins,
                targetRoomName = bestRoom.name,
                assignedTherapistName = session.therapistName,
                adviceMessage = advice
            )

            // Push forward this room's simulated availability for this client's full treatment duration!
            val sessionTotalDurationMs = session.totalDurationMinutes * 60 * 1000L
            simulatedTimeline[bestRoom.id] = availableAt + sessionTotalDurationMs + turnoverMs
        }

        // Wait time for a brand-new walk-in arriving right now
        val nextWalkInAvailableAt = rooms.minOfOrNull { simulatedTimeline[it.id] ?: now } ?: now
        val nextWalkInWaitMs = (nextWalkInAvailableAt - now).coerceAtLeast(0L)
        val nextWalkInWaitMinutes = if (nextWalkInWaitMs <= 0L) 0 else ((nextWalkInWaitMs + 59_999L) / 60_000L).toInt()

        val adviceMessage = when {
            !areAllRoomsBusy && waitingSessions.isEmpty() -> {
                "${freeRooms.size} suite(s) available now. Next therapist up: ${nextAvailableTherapist?.name ?: "None on deck"}."
            }
            waitingSessions.isNotEmpty() -> {
                "${waitingSessions.size} client(s) currently waiting in queue. Next walk-in estimated wait: ~$nextWalkInWaitMinutes min. Recommend 15m Steam Bath while waiting!"
            }
            earliestRoomName != null -> {
                "All suites busy. Next room free is $earliestRoomName in ~$earliestWaitMinutes min (Therapist: ${earliestTherapistName ?: "Assigned"})."
            }
            else -> {
                "All rooms currently assigned. Preparing next available slot."
            }
        }

        return WaitTimeEstimate(
            areAllRoomsBusy = areAllRoomsBusy,
            totalRooms = rooms.size,
            freeRoomsCount = freeRooms.size,
            inUseRoomsCount = rooms.size - freeRooms.size,
            waitingClientsCount = waitingSessions.size,
            earliestRoomFreeName = earliestRoomName,
            earliestRoomTherapistName = earliestTherapistName,
            minutesUntilEarliestRoomFree = earliestWaitMinutes,
            nextWalkInWaitMinutes = nextWalkInWaitMinutes,
            nextTherapistInTurn = nextAvailableTherapist,
            steamLoungeCapacityAvailable = true,
            adviceMessage = adviceMessage,
            clientWaitEstimates = clientWaitEstimates
        )
    }
}
