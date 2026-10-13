package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String = "",
    val phone: String = "",
    val isDefault: Boolean = false
)

enum class RoomType {
    SINGLE,
    COUPLE,
    STEAM_BATH
}

enum class RoomStatus {
    FREE,
    IN_USE,
    CLEANING,
    RESERVED
}

@Entity(tableName = "spa_rooms")
data class SpaRoomEntity(
    @PrimaryKey val id: String,
    val branchId: String,
    val name: String,
    val roomType: String = RoomType.SINGLE.name,
    val capacity: Int = 1,
    val status: String = RoomStatus.FREE.name,
    val currentSessionId: String? = null,
    val cleaningUntilEpoch: Long? = null
)

enum class TherapistStatus {
    AVAILABLE,
    IN_ROOM,
    BOOKED,
    ON_BREAK
}

@Entity(tableName = "therapists")
data class TherapistEntity(
    @PrimaryKey val id: String,
    val branchId: String,
    val name: String,
    val specialties: String = "Swedish, Deep Tissue",
    val phone: String = "",
    val orderIndex: Int = 0,
    val status: String = TherapistStatus.AVAILABLE.name,
    val activeSessionId: String? = null,
    val currentRoomName: String? = null,
    val completedSessionsToday: Int = 0,
    val isCheckedInToday: Boolean = true
)

@Entity(tableName = "treatments")
data class TreatmentEntity(
    @PrimaryKey val id: String,
    val branchId: String,
    val name: String,
    val category: String = "Massage",
    val durationMinutes: Int = 60,
    val price: Double = 50.0,
    val description: String = "",
    val hasSteamBetween: Boolean = false
)

enum class SessionStatus {
    WAITING,
    IN_PRE_STEAM,
    READY_FOR_ROOM,
    IN_TREATMENT,
    COMPLETED,
    CANCELLED
}

@Entity(tableName = "client_sessions")
data class ClientSessionEntity(
    @PrimaryKey val id: String,
    val branchId: String,
    val cardNumber: String,
    val clientName: String,
    val clientPhone: String = "",
    val isCouple: Boolean = false,
    val secondaryClientName: String? = null,
    val treatmentIds: String = "",
    val treatmentNames: String = "",
    val therapistId: String = "",
    val therapistName: String = "",
    val secondaryTherapistId: String? = null,
    val secondaryTherapistName: String? = null,
    val roomId: String = "",
    val roomName: String = "",
    val hasSteamBetween: Boolean = false,
    val hasPreSteam: Boolean = false,
    val preSteamDurationMinutes: Int = 15,
    val preSteamStartedAt: Long? = null,
    val preSteamFinishedAt: Long? = null,
    val totalDurationMinutes: Int = 60,
    val checkInTime: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val status: String = SessionStatus.WAITING.name,
    val addedOvertimeMinutes: Int = 0,
    val notes: String = ""
)

enum class BookingStatus {
    SCHEDULED,
    CHECKED_IN,
    CANCELLED
}

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val branchId: String,
    val clientName: String,
    val clientPhone: String = "",
    val therapistId: String = "",
    val therapistName: String = "",
    val targetRoomType: String = RoomType.SINGLE.name,
    val assignedRoomId: String? = null,
    val scheduledTimeEpoch: Long = System.currentTimeMillis(),
    val treatmentSummary: String = "",
    val durationMinutes: Int = 60,
    val status: String = BookingStatus.SCHEDULED.name,
    val notes: String = ""
)

@Entity(tableName = "branch_settings")
data class BranchSettingsEntity(
    @PrimaryKey val branchId: String,
    val cardStartNumber: Int = 101,
    val cardEndNumber: Int = 200,
    val cardPrefix: String = "SP-",
    val steamBetweenExtraMinutes: Int = 15,
    val defaultPreSteamMinutes: Int = 15,
    val roomCleaningBufferMinutes: Int = 5,
    val autoSuggestNextTherapist: Boolean = true
)
