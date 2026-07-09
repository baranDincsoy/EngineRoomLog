package com.example.engineroomlog.ui.managegroups

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.GroupWithParameters
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.model.OperationalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageGroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val groupDao = db.parameterGroupDao()
    private val paramDao = db.parameterDao()

    private var activeVesselId: Long = 1L

    private val _groups = MutableStateFlow<List<GroupWithParameters>>(emptyList())
    val groups: StateFlow<List<GroupWithParameters>> = _groups.asStateFlow()

    init {
        viewModelScope.launch {
            val vessel = db.vesselProfileDao().getActiveVessels().first().firstOrNull()
            if (vessel != null) activeVesselId = vessel.id

            groupDao.getGroupsWithParameters(activeVesselId).collect { list ->
                _groups.value = list.map { gwp ->
                    gwp.copy(
                        parameters = gwp.parameters
                            .filter { it.isActive }
                            .sortedBy { it.displayOrder }
                    )
                }
            }
        }
    }

    fun renameGroup(group: ParameterGroupEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            groupDao.update(group.copy(name = trimmed))
        }
    }

    fun deactivateGroup(group: ParameterGroupEntity) {
        viewModelScope.launch {
            groupDao.update(group.copy(isActive = false))
        }
    }

    fun moveParameter(parameter: ParameterEntity, targetGroupId: Long) {
        viewModelScope.launch {
            val nextOrder = (paramDao.getMaxDisplayOrder(targetGroupId) ?: -1) + 1
            paramDao.update(
                parameter.copy(groupId = targetGroupId, displayOrder = nextOrder)
            )
        }
    }

    fun moveParameterUp(groupParams: List<ParameterEntity>, index: Int) {
        if (index <= 0) return
        viewModelScope.launch {
            paramDao.swapDisplayOrder(groupParams[index], groupParams[index - 1])
        }
    }

    fun moveParameterDown(groupParams: List<ParameterEntity>, index: Int) {
        if (index >= groupParams.lastIndex) return
        viewModelScope.launch {
            paramDao.swapDisplayOrder(groupParams[index], groupParams[index + 1])
        }
    }

    fun moveGroupUp(group: ParameterGroupEntity) = moveGroup(group, -1)
    fun moveGroupDown(group: ParameterGroupEntity) = moveGroup(group, +1)

    private fun moveGroup(group: ParameterGroupEntity, direction: Int) {
        viewModelScope.launch {
            // Take a snapshot of groups in their current visual order
            val sorted = groups.value
                .map { it.group }          // GroupWithParameters -> entity; adjust to your type
                .sortedBy { it.displayOrder }

            val index = sorted.indexOfFirst { it.id == group.id }
            val neighborIndex = index + direction

            // Guard: item not found, or already at top/bottom -> do nothing
            if (index == -1 || neighborIndex !in sorted.indices) return@launch

            val neighbor = sorted[neighborIndex]

            // Swap displayOrder values; Flow will re-emit the new order
            groupDao.update(group.copy(displayOrder = neighbor.displayOrder))
            groupDao.update(neighbor.copy(displayOrder = group.displayOrder))
        }
    }

    fun updateParameter(
        parameter: ParameterEntity,
        newName: String,
        newUnit: String?,
        newState: OperationalState
    ) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            paramDao.update(
                parameter.copy(
                    name = trimmed,
                    unit = newUnit?.trim()?.ifEmpty { null },
                    state = newState
                )
            )
        }
    }
}