package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.engineroomlog.data.local.model.Permission

// One row per (vessel, rank, permission) that is GRANTED.
// Absence of a row means "not granted" — we only store the trues.
@Entity(
    tableName = "rank_permissions",
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
        Index(value = ["vesselProfileId", "rank", "permission"], unique = true)
    ]
)
data class RankPermissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vesselProfileId: Long,
    val rank: String,
    val permission: Permission
)