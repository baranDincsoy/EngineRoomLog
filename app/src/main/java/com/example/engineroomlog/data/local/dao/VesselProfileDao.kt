package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.engineroomlog.data.local.entity.VesselProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VesselProfileDao {

    @Insert
    suspend fun insert(vessel: VesselProfileEntity): Long

    @Query("SELECT * FROM vessel_profiles WHERE isActive = 1 ORDER BY name")
    fun getActiveVessels(): Flow<List<VesselProfileEntity>>

    @Query("SELECT * FROM vessel_profiles WHERE id = :id")
    suspend fun getById(id: Long): VesselProfileEntity?
}