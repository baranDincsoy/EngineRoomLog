package com.example.engineroomlog.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

// Room fills this: one group together with all its parameters, in one query
data class GroupWithParameters(
    @Embedded
    val group: ParameterGroupEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val parameters: List<ParameterEntity>
)