package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crew_members",
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
        Index(value = ["username"], unique = true)
    ]
)
data class CrewMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vesselProfileId: Long,
    val name: String,
    val rank: String,          // was String?, now required — drives permissions
    val username: String,
    val passwordHash: String,
    val isActive: Boolean = true
)