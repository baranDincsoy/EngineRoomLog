package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parameters")
data class ParameterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val unit: String?,
    val displayOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean = true
)