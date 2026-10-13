package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.BranchEntity
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.ClientSessionEntity
import com.example.data.model.SpaRoomEntity
import com.example.data.model.TherapistEntity
import com.example.data.model.TreatmentEntity

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
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpaDatabase::class.java,
                    "spa_flow_database.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
