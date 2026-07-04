package com.example.engineroomlog.ui.logentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
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
}