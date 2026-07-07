package com.example.engineroomlog.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

// Room fills this: one log entry together with all its readings, in one query
data class LogEntryWithReadings(
    @Embedded
    val entry: LogEntryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "logEntryId"
    )
    val readings: List<ReadingEntity>
)