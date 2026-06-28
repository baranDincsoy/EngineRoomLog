package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.OperationalState

@Entity(tableName = "parameters")
data class ParameterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val unit: String?,
    val state: OperationalState,
    val cadence: Cadence,
    val displayOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean = true
)