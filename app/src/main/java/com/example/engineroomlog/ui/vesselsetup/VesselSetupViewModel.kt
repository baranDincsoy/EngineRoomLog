package com.example.engineroomlog.ui.vesselsetup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.VesselProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VesselSetupViewModel(application: Application) : AndroidViewModel(application) {

    // DAO obtained from the single database instance (manual DI for now)
    private val vesselDao =
        DatabaseProvider.getDatabase(application).vesselProfileDao()

    private val _uiState = MutableStateFlow(VesselSetupUiState())
    val uiState: StateFlow<VesselSetupUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onImoChange(value: String) {
        _uiState.update { it.copy(imoNumber = value) }
    }

    fun saveVessel() {
        val currentName = _uiState.value.name.trim()
        if (currentName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vessel name is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val vessel = VesselProfileEntity(
                name = currentName,
                imoNumber = _uiState.value.imoNumber.trim().ifEmpty { null }
            )
            val newId = vesselDao.insert(vessel)
            _uiState.update { it.copy(isSaving = false, savedVesselId = newId) }
        }
    }
}