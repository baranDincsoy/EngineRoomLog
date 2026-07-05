package com.example.engineroomlog.ui.logentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.LogEntryEntity
import com.example.engineroomlog.data.local.entity.ReadingEntity
import com.example.engineroomlog.data.local.model.EntryStatus
import com.example.engineroomlog.data.local.model.OperationalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val groupDao = db.parameterGroupDao()
    private val logEntryDao = db.logEntryDao()
    private val readingDao = db.readingDao()

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    init {
        observeGroups()
        observeTodaysEntries()
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

            logEntryDao.getEntriesInRange(1, startOfDay, endOfDay).collect { entries ->
                _uiState.update { it.copy(todaysEntries = entries) }
            }
        }
    }

    private fun observeGroups() {
        viewModelScope.launch {
            groupDao.getGroupsWithParameters(1).collect { groups ->
                val sorted = groups.map { gwp ->
                    gwp.copy(parameters = gwp.parameters.sortedBy { it.displayOrder })
                }
                _uiState.update { it.copy(groups = sorted) }
            }
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
                watch = "12-16",                      // TODO: derive from clock later
                state = OperationalState.AT_SEA,      // TODO: sea/port toggle later
                status = EntryStatus.COLLECTING,
                collectedByName = "Test Admin",       // TODO: from logged-in user later
                collectedByCrewId = 1,
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

