package com.example.engineroomlog.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    // v1 -> v2: add nullable watch column to log_entries.
    // Nullable TEXT needs no DEFAULT; existing rows will hold NULL.
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE log_entries ADD COLUMN watch TEXT")
        }
    }

    // The single database instance for the whole app
    @Volatile
    private var instance: EngineRoomDatabase? = null

    fun getDatabase(context: Context): EngineRoomDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                EngineRoomDatabase::class.java,
                "engine_room_log.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}