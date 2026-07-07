package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.engineroomlog.data.local.entity.LogEntryEntity
import com.example.engineroomlog.data.local.entity.LogEntryWithReadings
import com.example.engineroomlog.data.local.entity.ReadingEntity
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

    @Transaction
    suspend fun insertEntryWithReadings(
        logEntry: LogEntryEntity,
        readings: List<ReadingEntity>,
        readingDao: ReadingDao
    ): Long {
        val newLogEntryId = insert(logEntry)
        val readingsWithId = readings.map { reading ->
            reading.copy(logEntryId = newLogEntryId)
        }
        readingDao.insertAll(readingsWithId)
        return newLogEntryId
    }

    // A day's journal: entries with their readings, oldest first (journal order)
    @Transaction
    @Query(
        "SELECT * FROM log_entries " +
                "WHERE vesselProfileId = :vesselId " +
                "AND timestamp BETWEEN :startMillis AND :endMillis " +
                "AND isArchived = 0 " +
                "ORDER BY timestamp ASC"
    )
    fun getJournalForRange(
        vesselId: Long,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<LogEntryWithReadings>>

}