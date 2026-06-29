package com.example.engineroomlog.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.engineroomlog.data.local.converter.Converters
import com.example.engineroomlog.data.local.dao.CrewMemberDao
import com.example.engineroomlog.data.local.dao.LogEntryDao
import com.example.engineroomlog.data.local.dao.ParameterDao
import com.example.engineroomlog.data.local.dao.ParameterGroupDao
import com.example.engineroomlog.data.local.dao.ReadingDao
import com.example.engineroomlog.data.local.dao.VesselProfileDao
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.entity.LogEntryEntity
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.entity.ReadingEntity
import com.example.engineroomlog.data.local.entity.VesselProfileEntity

@Database(
    entities = [
        VesselProfileEntity::class,
        ParameterGroupEntity::class,
        ParameterEntity::class,
        CrewMemberEntity::class,
        LogEntryEntity::class,
        ReadingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EngineRoomDatabase : RoomDatabase() {
    abstract fun vesselProfileDao(): VesselProfileDao
    abstract fun parameterGroupDao(): ParameterGroupDao
    abstract fun parameterDao(): ParameterDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun readingDao(): ReadingDao
    abstract fun crewMemberDao(): CrewMemberDao
}
