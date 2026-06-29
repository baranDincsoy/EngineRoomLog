package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParameterGroupDao {

    @Insert
    suspend fun insert(group: ParameterGroupEntity): Long

    @Update
    suspend fun update(group: ParameterGroupEntity)

    @Query(
        "SELECT * FROM parameter_groups " +
                "WHERE vesselProfileId = :vesselId AND isActive = 1 " +
                "ORDER BY displayOrder"
    )
    fun getGroupsForVessel(vesselId: Long): Flow<List<ParameterGroupEntity>>
}