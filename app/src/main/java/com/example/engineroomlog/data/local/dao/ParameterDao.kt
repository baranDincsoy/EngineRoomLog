package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.engineroomlog.data.local.entity.ParameterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParameterDao {

    @Insert
    suspend fun insert(parameter: ParameterEntity): Long

    @Update
    suspend fun update(parameter: ParameterEntity)

    // All active parameters of a group, in display order
    @Query(
        "SELECT * FROM parameters " +
                "WHERE groupId = :groupId AND isActive = 1 " +
                "ORDER BY displayOrder"
    )
    fun getParametersForGroup(groupId: Long): Flow<List<ParameterEntity>>

    @Query("SELECT MAX(displayOrder) FROM parameters WHERE groupId = :groupId")
    suspend fun getMaxDisplayOrder(groupId: Long): Int?

}