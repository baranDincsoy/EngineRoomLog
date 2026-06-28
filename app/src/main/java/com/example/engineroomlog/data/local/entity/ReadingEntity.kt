package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "readings",
    foreignKeys = [
        ForeignKey(
            entity = LogEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["logEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParameterEntity::class,
            parentColumns = ["id"],
            childColumns = ["parameterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("logEntryId"),
        Index("parameterId")
    ]
)
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val logEntryId: Long,
    val parameterId: Long,
    // Stored as text to preserve exactly what was entered (audit fidelity)
    val value: String
)