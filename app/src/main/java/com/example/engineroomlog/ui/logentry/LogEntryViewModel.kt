package com.example.engineroomlog.ui.logentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.GroupWithParameters
import com.example.engineroomlog.data.local.entity.LogEntryEntity
import com.example.engineroomlog.data.local.entity.ParameterEntity
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
    private val paramDao = db.parameterDao()
    private val logEntryDao = db.logEntryDao()
    private val readingDao = db.readingDao()

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    init {
        loadGroupsAndParameters()
    }

    private fun loadGroupsAndParameters() {
        viewModelScope.launch {
            groupDao.getGroupsForVessel(1).collect { groups ->
                val packed = groups.map { group ->
                    GroupWithParameters(
                        group = group,
                        parameters = paramDao
                            .getParametersForGroup(group.id)
                            .let { flow ->
                                var result = emptyList<ParameterEntity>()
                                flow.collect { result = it; return@collect }
                                result
                            }
                    )
                }
                _uiState.update { it.copy(groups = packed) }
            }
        }
    }

    fun onValueChange(parameterId: Long, value: String) {
        _uiState.update {
            it.copy(draftValues = it.draftValues + (parameterId to value))
        }
    }
}