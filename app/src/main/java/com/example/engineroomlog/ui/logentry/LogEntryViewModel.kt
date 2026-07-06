package com.example.engineroomlog.ui.logentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.entity.LogEntryEntity
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.entity.ReadingEntity
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.EntryStatus
import com.example.engineroomlog.data.local.model.OperationalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val groupDao = db.parameterGroupDao()
    private val logEntryDao = db.logEntryDao()
    private val readingDao = db.readingDao()

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()
    private var activeCrew: CrewMemberEntity? = null
    private val paramDao = db.parameterDao()
    private var activeVesselId: Long = 1L

    fun setActiveCrew(crewId: Long) {
        viewModelScope.launch {
            activeCrew = db.crewMemberDao().getById(crewId)
        }
    }
    init {
        viewModelScope.launch {
            // Resolve the real vessel id once, before starting any observers
            val vessel = db.vesselProfileDao().getActiveVessels().first().firstOrNull()
            if (vessel != null) {
                activeVesselId = vessel.id
            }
            android.util.Log.d("EngineRoomLog", "active vessel id = $activeVesselId")
            observeGroups()
            observeTodaysEntries()
        }
    }
    private fun observeTodaysEntries() {
        viewModelScope.launch {
            val startOfDay = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000

            logEntryDao.getEntriesInRange(activeVesselId, startOfDay, endOfDay).collect { entries ->
                _uiState.update { it.copy(todaysEntries = entries) }
            }
        }
    }
    fun deactivateParameter(parameter: ParameterEntity) {
        android.util.Log.d("EngineRoomLog", "deactivate: ${parameter.name}")
        viewModelScope.launch {
            paramDao.update(parameter.copy(isActive = false))
        }
    }
    fun onStateSelected(state: OperationalState) {
        _uiState.update { it.copy(selectedState = state) }
    }
    private fun observeGroups() {
        viewModelScope.launch {
            groupDao.getGroupsWithParameters(activeVesselId).collect { groups ->
                val sorted = groups.map { gwp ->
                    gwp.copy(parameters = gwp.parameters.sortedBy { it.displayOrder })
                }
                _uiState.update { it.copy(groups = sorted) }
            }
        }
    }

    fun addGroup(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        viewModelScope.launch {
            try {
                val nextOrder = (groupDao.getMaxDisplayOrder(activeVesselId) ?: -1) + 1
                val newId = groupDao.insert(
                    ParameterGroupEntity(
                        vesselProfileId = activeVesselId,
                        name = trimmedName,
                        displayOrder = nextOrder
                    )
                )
                _uiState.update { it.copy(lastCreatedGroupId = newId) }
                android.util.Log.d("EngineRoomLog", "Group inserted with id=$newId")
            } catch (e: Exception) {
                android.util.Log.e("EngineRoomLog", "addGroup failed", e)
            }
        }
    }
    fun addParameter(
        groupId: Long,
        name: String,
        unit: String?,
        state: OperationalState
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        viewModelScope.launch {
            val nextOrder = (paramDao.getMaxDisplayOrder(groupId) ?: -1) + 1
            paramDao.insert(
                ParameterEntity(
                    groupId = groupId,
                    name = trimmedName,
                    unit = unit?.trim()?.ifEmpty { null },
                    state = state,
                    cadence = Cadence.HOURLY,
                    displayOrder = nextOrder,
                    isDefault = false
                )
            )
        }
    }
    fun onValueChange(parameterId: Long, value: String) {
        _uiState.update {
            it.copy(draftValues = it.draftValues + (parameterId to value))
        }
    }

    fun saveEntry() {
        val drafts = _uiState.value.draftValues.filterValues { it.isNotBlank() }

        if (drafts.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter at least one value") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val entry = LogEntryEntity(

                vesselProfileId = 1,
                timestamp = System.currentTimeMillis(),
                state = _uiState.value.selectedState,      // TODO: sea/port toggle later
                status = EntryStatus.COLLECTING,
                collectedByName = activeCrew?.name ?: "Unknown",
                collectedByCrewId = activeCrew?.id,
                collectedAt = System.currentTimeMillis(),
                postedByName = null,
                postedByCrewId = null,
                postedAt = null,
                remarks = null
            )

            val readings = drafts.map { (parameterId, value) ->
                ReadingEntity(
                    logEntryId = 0,   // real id is stamped inside the transaction
                    parameterId = parameterId,
                    value = value.trim()
                )
            }

            val entryId = logEntryDao.insertEntryWithReadings(entry, readings, readingDao)

            _uiState.update {
                it.copy(isSaving = false, savedEntryId = entryId, draftValues = emptyMap())
            }
        }
    }

}

