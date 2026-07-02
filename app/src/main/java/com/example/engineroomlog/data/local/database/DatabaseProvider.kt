package com.example.engineroomlog.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    // The single database instance for the whole app
    @Volatile
    private var instance: EngineRoomDatabase? = null

    fun getDatabase(context: Context): EngineRoomDatabase {
        // Return the existing instance, or create it once if it doesn't exist yet
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                EngineRoomDatabase::class.java,
                "engine_room_log.db"
            ).build().also { instance = it }
        }
    }
}