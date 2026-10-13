package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.BranchEntity
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.ClientSessionEntity
import com.example.data.model.RoomStatus
import com.example.data.model.RoomType
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TherapistStatus
import com.example.data.model.TreatmentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BranchEntity::class,
        SpaRoomEntity::class,
        TherapistEntity::class,
        TreatmentEntity::class,
        ClientSessionEntity::class,
        BookingEntity::class,
        BranchSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpaDatabase : RoomDatabase() {
    abstract fun spaDao(): SpaDao

    companion object {
        @Volatile
        private var INSTANCE: SpaDatabase? = null

        fun getDatabase(context: Context): SpaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpaDatabase::class.java,
                    "spa_flow_database.db"
                )
                    .addCallback(DatabaseSeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database.spaDao())
                }
            }
        }

        private suspend fun seedDatabase(dao: SpaDao) {
            val mainBranchId = "branch_downtown"
            val highlandBranchId = "branch_highland"

            val branches = listOf(
                BranchEntity(
                    id = mainBranchId,
                    name = "Downtown Oasis Spa",
                    address = "450 Serenity Ave, Grand Plaza",
                    phone = "+1 (555) 019-2831",
                    isDefault = true
                ),
                BranchEntity(
                    id = highlandBranchId,
                    name = "Highland Wellness Retreat",
                    address = "12 Hilltop Way, Skyline Terrace",
                    phone = "+1 (555) 014-9844",
                    isDefault = false
                )
            )
            dao.insertBranches(branches)

            // Settings for Downtown
            dao.insertOrUpdateSettings(
                BranchSettingsEntity(
                    branchId = mainBranchId,
                    cardStartNumber = 101,
                    cardEndNumber = 200,
                    cardPrefix = "SP-",
                    steamBetweenExtraMinutes = 15,
                    defaultPreSteamMinutes = 15,
                    roomCleaningBufferMinutes = 5,
                    autoSuggestNextTherapist = true
                )
            )
            dao.insertOrUpdateSettings(
                BranchSettingsEntity(
                    branchId = highlandBranchId,
                    cardStartNumber = 301,
                    cardEndNumber = 400,
                    cardPrefix = "HL-",
                    steamBetweenExtraMinutes = 15,
                    defaultPreSteamMinutes = 20,
                    roomCleaningBufferMinutes = 5,
                    autoSuggestNextTherapist = true
                )
            )

            // Seed Rooms for Downtown
            val rooms = listOf(
                SpaRoomEntity(
                    id = "room_1",
                    branchId = mainBranchId,
                    name = "Room 1 (Serenity Single)",
                    roomType = RoomType.SINGLE.name,
                    capacity = 1,
                    status = RoomStatus.FREE.name
                ),
                SpaRoomEntity(
                    id = "room_2",
                    branchId = mainBranchId,
                    name = "Room 2 (Zen Garden Single)",
                    roomType = RoomType.SINGLE.name,
                    capacity = 1,
                    status = RoomStatus.FREE.name
                ),
                SpaRoomEntity(
                    id = "room_3",
                    branchId = mainBranchId,
                    name = "Room 3 (Bamboo Forest Single)",
                    roomType = RoomType.SINGLE.name,
                    capacity = 1,
                    status = RoomStatus.FREE.name
                ),
                SpaRoomEntity(
                    id = "room_4",
                    branchId = mainBranchId,
                    name = "Room 4 (Lotus Flower Single)",
                    roomType = RoomType.SINGLE.name,
                    capacity = 1,
                    status = RoomStatus.FREE.name
                ),
                SpaRoomEntity(
                    id = "room_5",
                    branchId = mainBranchId,
                    name = "Couples Suite 1 (Harmony Suite)",
                    roomType = RoomType.COUPLE.name,
                    capacity = 2,
                    status = RoomStatus.FREE.name
                ),
                SpaRoomEntity(
                    id = "room_6",
                    branchId = mainBranchId,
                    name = "Couples Suite 2 (Elysium Sanctuary)",
                    roomType = RoomType.COUPLE.name,
                    capacity = 2,
                    status = RoomStatus.FREE.name
                ),
                SpaRoomEntity(
                    id = "room_steam",
                    branchId = mainBranchId,
                    name = "Thermal Steam Bath Lounge",
                    roomType = RoomType.STEAM_BATH.name,
                    capacity = 6,
                    status = RoomStatus.FREE.name
                )
            )
            dao.insertRooms(rooms)

            // Seed Therapists in Manager rotation order
            val therapists = listOf(
                TherapistEntity(
                    id = "th_1",
                    branchId = mainBranchId,
                    name = "Maya Chen",
                    specialties = "Deep Tissue, Hot Stone, Swedish",
                    phone = "555-0101",
                    orderIndex = 1,
                    status = TherapistStatus.AVAILABLE.name
                ),
                TherapistEntity(
                    id = "th_2",
                    branchId = mainBranchId,
                    name = "Lucas Miller",
                    specialties = "Swedish, Sports Recovery, Aromatherapy",
                    phone = "555-0102",
                    orderIndex = 2,
                    status = TherapistStatus.AVAILABLE.name
                ),
                TherapistEntity(
                    id = "th_3",
                    branchId = mainBranchId,
                    name = "Priya Sharma",
                    specialties = "Ayurvedic, Balinese, Herbal Scrub",
                    phone = "555-0103",
                    orderIndex = 3,
                    status = TherapistStatus.AVAILABLE.name
                ),
                TherapistEntity(
                    id = "th_4",
                    branchId = mainBranchId,
                    name = "Elena Vance",
                    specialties = "Facials, Shiatsu, Head & Neck",
                    phone = "555-0104",
                    orderIndex = 4,
                    status = TherapistStatus.AVAILABLE.name
                ),
                TherapistEntity(
                    id = "th_5",
                    branchId = mainBranchId,
                    name = "Tariq Rahman",
                    specialties = "Reflexology, Deep Tissue, Thai",
                    phone = "555-0105",
                    orderIndex = 5,
                    status = TherapistStatus.AVAILABLE.name
                ),
                TherapistEntity(
                    id = "th_6",
                    branchId = mainBranchId,
                    name = "Grace O'Connor",
                    specialties = "Couples Rituals, Aromatherapy, Scrub",
                    phone = "555-0106",
                    orderIndex = 6,
                    status = TherapistStatus.AVAILABLE.name
                )
            )
            dao.insertTherapists(therapists)

            // Seed Treatments
            val treatments = listOf(
                TreatmentEntity(
                    id = "tr_1",
                    branchId = mainBranchId,
                    name = "Deep Tissue Massage",
                    category = "Massage",
                    durationMinutes = 60,
                    price = 75.0,
                    description = "Targeted pressure for chronic muscle tension and knots"
                ),
                TreatmentEntity(
                    id = "tr_2",
                    branchId = mainBranchId,
                    name = "Swedish Relaxation Massage",
                    category = "Massage",
                    durationMinutes = 60,
                    price = 65.0,
                    description = "Gentle, flowing strokes for full body relaxation"
                ),
                TreatmentEntity(
                    id = "tr_3",
                    branchId = mainBranchId,
                    name = "Aromatherapy Bliss Massage",
                    category = "Massage",
                    durationMinutes = 75,
                    price = 85.0,
                    description = "Essential botanicals customized for stress release"
                ),
                TreatmentEntity(
                    id = "tr_4",
                    branchId = mainBranchId,
                    name = "Hydra-Glow Botanical Facial",
                    category = "Facial",
                    durationMinutes = 45,
                    price = 55.0,
                    description = "Deep cleansing, organic exfoliation and hydrating mask"
                ),
                TreatmentEntity(
                    id = "tr_5",
                    branchId = mainBranchId,
                    name = "Himalayan Herbal Body Scrub",
                    category = "Body Scrub",
                    durationMinutes = 45,
                    price = 60.0,
                    description = "Mineral-rich exfoliating body polish with warm essential oils"
                ),
                TreatmentEntity(
                    id = "tr_6",
                    branchId = mainBranchId,
                    name = "Foot & Leg Reflexology",
                    category = "Reflexology",
                    durationMinutes = 30,
                    price = 40.0,
                    description = "Acupressure meridian pressure on feet and lower calves"
                ),
                TreatmentEntity(
                    id = "tr_7",
                    branchId = mainBranchId,
                    name = "Couples Harmony Ritual",
                    category = "Couples",
                    durationMinutes = 90,
                    price = 160.0,
                    description = "Synchronized couples massage in private luxury suite"
                ),
                TreatmentEntity(
                    id = "tr_8",
                    branchId = mainBranchId,
                    name = "Royal Renewal Package",
                    category = "Package",
                    durationMinutes = 120,
                    price = 145.0,
                    description = "Body scrub + Swedish massage + Botanical facial",
                    hasSteamBetween = true
                )
            )
            dao.insertTreatments(treatments)
        }
    }
}
