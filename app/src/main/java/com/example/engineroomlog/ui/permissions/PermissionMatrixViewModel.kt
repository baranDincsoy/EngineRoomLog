package com.example.engineroomlog.ui.permissions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.RankPermissionEntity
import com.example.engineroomlog.data.local.model.Permission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// A single cell's identity
data class Cell(val rank: String, val permission: Permission)

class PermissionMatrixViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private var vesselId: Long = 1L

    // What's in the DB right now
    private val _saved = MutableStateFlow<Set<Cell>>(emptySet())
    val saved: StateFlow<Set<Cell>> = _saved.asStateFlow()

    // What the user is editing (starts equal to saved)
    private val _draft = MutableStateFlow<Set<Cell>>(emptySet())
    val draft: StateFlow<Set<Cell>> = _draft.asStateFlow()

    val hasChanges: Boolean get() = _saved.value != _draft.value

    init {
        viewModelScope.launch {
            val vessel = db.vesselProfileDao().getActiveVessels().first().firstOrNull()
            if (vessel != null) vesselId = vessel.id

            val current = db.rankPermissionDao().getMatrix(vesselId).first()
                .map { Cell(it.rank, it.permission) }.toSet()
            _saved.value = current
            _draft.value = current
        }
    }

    // Toggle a cell in the draft only — DB untouched until save
    fun toggle(cell: Cell) {
        _draft.value = if (cell in _draft.value) _draft.value - cell else _draft.value + cell
    }

    fun isGranted(cell: Cell): Boolean = cell in _draft.value

    fun save() {
        viewModelScope.launch {
            val dao = db.rankPermissionDao()
            val toAdd = _draft.value - _saved.value
            val toRemove = _saved.value - _draft.value

            toAdd.forEach {
                dao.grant(RankPermissionEntity(vesselProfileId = vesselId, rank = it.rank, permission = it.permission))
            }
            toRemove.forEach {
                dao.revoke(vesselId, it.rank, it.permission)
            }
            _saved.value = _draft.value   // draft is now the truth
        }
    }

    fun discard() {
        _draft.value = _saved.value
    }
}