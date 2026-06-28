package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vessel_profiles")
data class VesselProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val imoNumber: String?,
    val retentionDays: Int = 365,
    val isActive: Boolean = true
)