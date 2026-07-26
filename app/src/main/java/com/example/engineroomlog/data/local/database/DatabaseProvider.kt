package com.example.engineroomlog.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    // v1 -> v2: add nullable watch column to log_entries.
    // Nullable TEXT needs no DEFAULT; existing rows will hold NULL.

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // SQLite can't drop a column: rebuild the table without `role`
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `crew_members_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`vesselProfileId` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`rank` TEXT NOT NULL, " +
                        "`username` TEXT NOT NULL, " +
                        "`passwordHash` TEXT NOT NULL, " +
                        "`isActive` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`vesselProfileId`) REFERENCES `vessel_profiles`(`id`) ON DELETE CASCADE)"
            )
            // Copy everything but role; any legacy null rank falls back to Oiler
            db.execSQL(
                "INSERT INTO `crew_members_new` " +
                        "(id, vesselProfileId, name, rank, username, passwordHash, isActive) " +
                        "SELECT id, vesselProfileId, name, COALESCE(rank, 'Oiler'), " +
                        "username, passwordHash, isActive FROM `crew_members`"
            )
            db.execSQL("DROP TABLE `crew_members`")
            db.execSQL("ALTER TABLE `crew_members_new` RENAME TO `crew_members`")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_crew_members_vesselProfileId` " +
                        "ON `crew_members` (`vesselProfileId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_crew_members_username` " +
                        "ON `crew_members` (`username`)"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE log_entries ADD COLUMN collectedByEmployeeNo TEXT")
            // Backfill existing entries from the crew table so old rows aren't blank
            db.execSQL(
                "UPDATE log_entries SET collectedByEmployeeNo = " +
                        "(SELECT username FROM crew_members WHERE crew_members.id = log_entries.collectedByCrewId) " +
                        "WHERE collectedByCrewId IS NOT NULL"
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
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .build()
                .also { instance = it }
        }
    }
}