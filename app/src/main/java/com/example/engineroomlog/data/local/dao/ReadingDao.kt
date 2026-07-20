package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.engineroomlog.data.local.entity.ReadingEntity

@Dao
interface ReadingDao {

    @Insert
    suspend fun insert(reading: ReadingEntity): Long

    // Insert many readings at once (all values of one log entry)
    @Insert
    suspend fun insertAll(readings: List<ReadingEntity>)

    // All raw readings belonging to one log entry
    @Query("SELECT * FROM readings WHERE logEntryId = :entryId")
    suspend fun getReadingsForEntry(entryId: Long): List<ReadingEntity>




}