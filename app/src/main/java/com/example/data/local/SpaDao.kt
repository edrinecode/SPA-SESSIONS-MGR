package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingEntity
import com.example.data.model.BranchEntity
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.ClientSessionEntity
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TreatmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaDao {
    // Branches
    @Query("SELECT * FROM branches ORDER BY name ASC")
    fun getAllBranches(): Flow<List<BranchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<BranchEntity>)

    // Rooms
    @Query("SELECT * FROM spa_rooms WHERE branchId = :branchId ORDER BY name ASC")
    fun getRoomsForBranch(branchId: String): Flow<List<SpaRoomEntity>>

    @Query("SELECT * FROM spa_rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: String): SpaRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: SpaRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<SpaRoomEntity>)

    @Update
    suspend fun updateRoom(room: SpaRoomEntity)

    @Delete
    suspend fun deleteRoom(room: SpaRoomEntity)

    // Therapists
    @Query("SELECT * FROM therapists WHERE branchId = :branchId ORDER BY orderIndex ASC")
    fun getTherapistsForBranch(branchId: String): Flow<List<TherapistEntity>>

    @Query("SELECT * FROM therapists WHERE id = :id LIMIT 1")
    suspend fun getTherapistById(id: String): TherapistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTherapist(therapist: TherapistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTherapists(therapists: List<TherapistEntity>)

    @Update
    suspend fun updateTherapist(therapist: TherapistEntity)

    @Update
    suspend fun updateTherapists(therapists: List<TherapistEntity>)

    @Delete
    suspend fun deleteTherapist(therapist: TherapistEntity)

    // Treatments
    @Query("SELECT * FROM treatments WHERE branchId = :branchId ORDER BY category, name ASC")
    fun getTreatmentsForBranch(branchId: String): Flow<List<TreatmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatment(treatment: TreatmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatments(treatments: List<TreatmentEntity>)

    @Update
    suspend fun updateTreatment(treatment: TreatmentEntity)

    @Delete
    suspend fun deleteTreatment(treatment: TreatmentEntity)

    // Client Sessions
    @Query("SELECT * FROM client_sessions WHERE branchId = :branchId AND status IN ('WAITING', 'IN_PRE_STEAM', 'READY_FOR_ROOM', 'IN_TREATMENT') ORDER BY checkInTime DESC")
    fun getActiveSessionsForBranch(branchId: String): Flow<List<ClientSessionEntity>>

    @Query("SELECT * FROM client_sessions WHERE branchId = :branchId ORDER BY checkInTime DESC LIMIT 50")
    fun getRecentSessionsForBranch(branchId: String): Flow<List<ClientSessionEntity>>

    @Query("SELECT * FROM client_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ClientSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClientSessionEntity)

    @Update
    suspend fun updateSession(session: ClientSessionEntity)

    // Bookings
    @Query("SELECT * FROM bookings WHERE branchId = :branchId AND status = 'SCHEDULED' ORDER BY scheduledTimeEpoch ASC")
    fun getScheduledBookingsForBranch(branchId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE branchId = :branchId ORDER BY scheduledTimeEpoch DESC LIMIT 50")
    fun getAllBookingsForBranch(branchId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)

    // Branch Settings
    @Query("SELECT * FROM branch_settings WHERE branchId = :branchId LIMIT 1")
    fun getSettingsForBranch(branchId: String): Flow<BranchSettingsEntity?>

    @Query("SELECT * FROM branch_settings WHERE branchId = :branchId LIMIT 1")
    suspend fun getSettingsSync(branchId: String): BranchSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: BranchSettingsEntity)
}
