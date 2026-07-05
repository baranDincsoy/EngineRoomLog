package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrewMemberDao {

    @Insert
    suspend fun insert(crewMember: CrewMemberEntity): Long

    @Update
    suspend fun update(crewMember: CrewMemberEntity)

    // Used for login: find an active member by username
    @Query(
        "SELECT * FROM crew_members " +
                "WHERE username = :username AND isActive = 1 " +
                "LIMIT 1"
    )
    suspend fun findByUsername(username: String): CrewMemberEntity?

    @Query("SELECT * FROM crew_members WHERE id = :id")
    suspend fun getById(id: Long): CrewMemberEntity?

    // All active crew of a vessel, for the admin/management screen
    @Query(
        "SELECT * FROM crew_members " +
                "WHERE vesselProfileId = :vesselId AND isActive = 1 " +
                "ORDER BY name"
    )
    fun getActiveCrew(vesselId: Long): Flow<List<CrewMemberEntity>>
}