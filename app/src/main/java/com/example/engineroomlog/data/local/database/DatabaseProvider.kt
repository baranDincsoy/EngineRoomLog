package com.example.engineroomlog.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    // v1 -> v2: add nullable watch column to log_entries.
    // Nullable TEXT needs no DEFAULT; existing rows will hold NULL.

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `rank_permissions` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`vesselProfileId` INTEGER NOT NULL, " +
                        "`rank` TEXT NOT NULL, " +
                        "`permission` TEXT NOT NULL, " +
                        "FOREIGN KEY(`vesselProfileId`) REFERENCES `vessel_profiles`(`id`) ON DELETE CASCADE)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_rank_permissions_vesselProfileId` " +
                        "ON `rank_permissions` (`vesselProfileId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_rank_permissions_vesselProfileId_rank_permission` " +
                        "ON `rank_permissions` (`vesselProfileId`, `rank`, `permission`)"
            )
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
                .addMigrations(MIGRATION_3_4)
                .build()
                .also { instance = it }
        }
    }
}