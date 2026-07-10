package com.example.engineroomlog.ui.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val groupDao = db.parameterGroupDao()
    private val logEntryDao = db.logEntryDao()

    private var activeVesselId: Long = 1L

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    // Which day the journal shows; changing this re-drives the flow below
    private val dayStart = MutableStateFlow(startOfToday())

    init {
        viewModelScope.launch {
            val vessel = db.vesselProfileDao().getActiveVessels().first().firstOrNull()
            if (vessel != null) activeVesselId = vessel.id

            dayStart.collect { start ->
                observeDay(start)
            }
        }
    }

    private var dayJob: kotlinx.coroutines.Job? = null

    private fun observeDay(startMillis: Long) {
        dayJob?.cancel()
        dayJob = viewModelScope.launch {
            val endMillis = startMillis + 24L * 60 * 60 * 1000

            combine(
                groupDao.getGroupsWithParameters(activeVesselId),
                logEntryDao.getJournalForRange(activeVesselId, startMillis, endMillis)
            ) { groups, entries ->
                JournalUiState(
                    dayStartMillis = startMillis,
                    groups = groups.map { gwp ->
                        gwp.copy(
                            parameters = gwp.parameters
                                .filter { it.isActive }
                                .sortedBy { it.displayOrder }
                        )
                    },
                    rows = entries.map { ewr ->
                        JournalRow(
                            entryId = ewr.entry.id,
                            timestamp = ewr.entry.timestamp,
                            collectedByName = ewr.entry.collectedByName,
                            collectedByCrewId = ewr.entry.collectedByCrewId,
                            remarks = ewr.entry.remarks,
                            values = ewr.readings.associate { it.parameterId to it.value }
                        )
                    }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun goToPreviousDay() {
        dayStart.update { it - 24L * 60 * 60 * 1000 }
    }

    fun goToNextDay() {
        dayStart.update { it + 24L * 60 * 60 * 1000 }
    }

    private fun startOfToday(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}