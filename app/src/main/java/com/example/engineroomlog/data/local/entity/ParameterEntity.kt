package com.example.engineroomlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.OperationalState

@Entity(
    tableName = "parameters",
    foreignKeys = [
        ForeignKey(
            entity = ParameterGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
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