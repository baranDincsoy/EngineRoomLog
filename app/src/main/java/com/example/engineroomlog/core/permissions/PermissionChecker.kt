package com.example.engineroomlog.core.permissions

import android.content.Context
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.model.Permission
import kotlinx.coroutines.flow.first

// Single source of truth for "can this rank do this?" — reads the vessel's matrix.
object PermissionChecker {

    suspend fun can(context: Context, rank: String, permission: Permission): Boolean {
        val db = DatabaseProvider.getDatabase(context)
        val vessel = db.vesselProfileDao().getActiveVessels().first().firstOrNull()
            ?: return false
        return db.rankPermissionDao().hasPermission(vessel.id, rank, permission)
    }
}