package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parameter_groups")
data class ParameterGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vesselProfileId: Long,
    val name: String,
    val displayOrder: Int,
    val isActive: Boolean = true
)