package com.example.engineroomlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.engineroomlog.data.local.entity.RankPermissionEntity
import com.example.engineroomlog.data.local.model.Permission
import kotlinx.coroutines.flow.Flow

@Dao
interface RankPermissionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun grant(entry: RankPermissionEntity)

    @Query(
        "DELETE FROM rank_permissions " +
                "WHERE vesselProfileId = :vesselId AND rank = :rank AND permission = :permission"
    )
    suspend fun revoke(vesselId: Long, rank: String, permission: Permission)

    // The whole matrix for a vessel — drives the matrix screen
    @Query("SELECT * FROM rank_permissions WHERE vesselProfileId = :vesselId")
    fun getMatrix(vesselId: Long): Flow<List<RankPermissionEntity>>

    // The check the whole app will lean on: does this rank have this permission?
    @Query(
        "SELECT COUNT(*) > 0 FROM rank_permissions " +
                "WHERE vesselProfileId = :vesselId AND rank = :rank AND permission = :permission"
    )
    suspend fun hasPermission(vesselId: Long, rank: String, permission: Permission): Boolean

    // All permissions granted to a rank — resolved once after login
    @Query(
        "SELECT permission FROM rank_permissions " +
                "WHERE vesselProfileId = :vesselId AND rank = :rank"
    )
    suspend fun getPermissionsForRank(vesselId: Long, rank: String): List<Permission>

    // How many ACTIVE crew currently hold a given permission (via their rank)?
    // Used to prevent locking the vessel out of crew/permission management.
    @Query(
        "SELECT COUNT(*) FROM crew_members c " +
                "WHERE c.vesselProfileId = :vesselId AND c.isActive = 1 " +
                "AND EXISTS (SELECT 1 FROM rank_permissions rp " +
                "WHERE rp.vesselProfileId = :vesselId AND rp.rank = c.rank " +
                "AND rp.permission = :permission)"
    )
    suspend fun countCrewWithPermission(vesselId: Long, permission: Permission): Int
}