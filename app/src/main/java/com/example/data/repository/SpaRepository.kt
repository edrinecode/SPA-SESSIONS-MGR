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

data class WaitTimeEstimate(
    val areAllRoomsBusy: Boolean,
    val totalRooms: Int,
    val freeRoomsCount: Int,
    val inUseRoomsCount: Int,
    val earliestRoomFreeName: String?,
    val earliestRoomTherapistName: String?,
    val minutesUntilEarliestRoomFree: Int,
    val nextTherapistInTurn: TherapistEntity?,
    val steamLoungeCapacityAvailable: Boolean,
    val adviceMessage: String
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
        if (branches.isEmpty()) {
            val branchId = "branch_main"
            dao.insertBranch(
                BranchEntity(
                    id = branchId,
                    name = "Main Branch",
                    isDefault = true
                )
            )
            // Keep first launch honest: configure the spa's real rooms, treatments,
            // durations, and daily therapist roster in the app instead of showing demo data.
            dao.insertOrUpdateSettings(BranchSettingsEntity(branchId = branchId))
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

        val initialStatus = if (hasPreSteam) {
            SessionStatus.IN_PRE_STEAM.name
        } else {
            SessionStatus.READY_FOR_ROOM.name
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
            startedAt = null, // Will start when therapist calls receptionist!
            status = initialStatus,
            notes = notes
        )

        dao.insertSession(session)

        // If not doing pre-steam, room and therapist are claimed right away
        if (!hasPreSteam) {
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
        } else {
            // Client is in pre-steam. Hold the selected room and therapist for this visit
            // so another check-in cannot claim the same resources.
            dao.updateRoom(
                room.copy(
                    status = RoomStatus.RESERVED.name,
                    currentSessionId = sessionId
                )
            )
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
        }

        return session
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

    // Wait Time & Smart Receptionist Calculation
    suspend fun calculateWaitTimeInfo(branchId: String): WaitTimeEstimate {
        val rooms = dao.getRoomsForBranch(branchId).first().filter { it.roomType != RoomType.STEAM_BATH.name }
        val activeSessions = dao.getActiveSessionsForBranch(branchId).first()
        val therapists = dao.getTherapistsForBranch(branchId).first()

        val now = System.currentTimeMillis()
        val freeRooms = rooms.filter {
            it.status == RoomStatus.FREE.name && (it.cleaningUntilEpoch ?: 0L) <= now
        }
        val areAllRoomsBusy = freeRooms.isEmpty() && rooms.isNotEmpty()

        val nextAvailableTherapist = therapists
            .filter { it.status == TherapistStatus.AVAILABLE.name && it.isCheckedInToday }
            .minByOrNull { it.orderIndex }

        // Find which in-use room is freeing up soonest
        var earliestRemainingMinutes = Int.MAX_VALUE
        var earliestRoomName: String? = null
        var earliestTherapistName: String? = null

        // Rooms already in turnover are not free yet, but are often the next usable option.
        rooms.filter { it.status == RoomStatus.CLEANING.name }.forEach { room ->
            val remainingMs = ((room.cleaningUntilEpoch ?: now) - now).coerceAtLeast(0L)
            val remainingMins = ((remainingMs + 59_999L) / 60_000L).toInt()
            if (remainingMins < earliestRemainingMinutes) {
                earliestRemainingMinutes = remainingMins
                earliestRoomName = room.name
                earliestTherapistName = null
            }
        }

        val activeInTreatmentSessions = activeSessions.filter { it.status == SessionStatus.IN_TREATMENT.name }
        val settings = dao.getSettingsSync(branchId) ?: BranchSettingsEntity(branchId)
        val turnoverMs = settings.roomCleaningBufferMinutes.coerceAtLeast(0) * 60 * 1000L
        for (session in activeInTreatmentSessions) {
            val startTime = session.startedAt ?: session.checkInTime
            val totalAllowedMs = (session.totalDurationMinutes + session.addedOvertimeMinutes) * 60 * 1000L
            val elapsedMs = now - startTime
            val remainingMs = (totalAllowedMs - elapsedMs).coerceAtLeast(0L) + turnoverMs
            val remainingMins = ((remainingMs + 59_999L) / 60_000L).toInt()

            if (remainingMins < earliestRemainingMinutes) {
                earliestRemainingMinutes = remainingMins
                earliestRoomName = session.roomName
                earliestTherapistName = session.therapistName
            }
        }

        val advice = when {
            !areAllRoomsBusy -> {
                "${freeRooms.size} room(s) available now. Next therapist up: ${nextAvailableTherapist?.name ?: "None on deck"}."
            }
            earliestRoomName != null -> {
                "All rooms are occupied. Next room free is $earliestRoomName in ~$earliestRemainingMinutes mins (Therapist: $earliestTherapistName). You can recommend the 15-min Steam Bath while waiting!"
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
            earliestRoomFreeName = earliestRoomName,
            earliestRoomTherapistName = earliestTherapistName,
            minutesUntilEarliestRoomFree = if (earliestRemainingMinutes == Int.MAX_VALUE) 0 else earliestRemainingMinutes,
            nextTherapistInTurn = nextAvailableTherapist,
            steamLoungeCapacityAvailable = true,
            adviceMessage = advice
        )
    }
}
