package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.engineroomlog.data.local.entity.LogEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    @Insert
    suspend fun insert(logEntry: LogEntryEntity): Long

    @Update
    suspend fun update(logEntry: LogEntryEntity)

    // One specific log entry by id
    @Query("SELECT * FROM log_entries WHERE id = :id")
    suspend fun getById(id: Long): LogEntryEntity?

    // All entries for a vessel within a time range, newest first
    // Used to build a day's journal page
    @Query(
        "SELECT * FROM log_entries " +
                "WHERE vesselProfileId = :vesselId " +
                "AND timestamp BETWEEN :startMillis AND :endMillis " +
                "AND isArchived = 0 " +
                "ORDER BY timestamp DESC"
    )
    fun getEntriesInRange(
        vesselId: Long,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<LogEntryEntity>>
}