package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.engineroomlog.data.local.model.EntryStatus
import com.example.engineroomlog.data.local.model.OperationalState

@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = VesselProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["vesselProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("vesselProfileId"),
        Index("timestamp")
    ]
)
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vesselProfileId: Long,
    val timestamp: Long,
    val watch: String,
    val state: OperationalState,
    val status: EntryStatus,
    // Name is snapshotted at entry time, so history survives account deletion
    val collectedByName: String,
    val collectedByCrewId: Long?,
    val collectedAt: Long,
    val postedByName: String?,
    val postedByCrewId: Long?,
    val postedAt: Long?,
    val remarks: String?,
    val isArchived: Boolean = false
)