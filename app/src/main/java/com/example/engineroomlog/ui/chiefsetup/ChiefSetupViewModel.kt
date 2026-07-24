package com.example.engineroomlog.ui.chiefsetup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.core.security.PasswordHasher
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.database.TemplateSeeder
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.model.Ranks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChiefSetupUiState(
    val name: String = "",
    val employeeNo: String = "",
    val password: String = "",
    val useSampleLayout: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val done: Boolean = false
)

class ChiefSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)

    private val _uiState = MutableStateFlow(ChiefSetupUiState())
    val uiState: StateFlow<ChiefSetupUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onEmployeeNoChange(v: String) = _uiState.update { it.copy(employeeNo = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onTemplateChange(useSample: Boolean) = _uiState.update { it.copy(useSampleLayout = useSample) }

    fun finishSetup(vesselId: Long) {
        val s = _uiState.value
        val name = s.name.trim()
        val no = s.employeeNo.trim()
        if (name.isEmpty() || no.isEmpty() || s.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            db.crewMemberDao().insert(
                CrewMemberEntity(
                    vesselProfileId = vesselId,
                    name = name,
                    rank = Ranks.CHIEF_ENGINEER,
                    username = no,
                    passwordHash = PasswordHasher.hash(s.password)
                )
            )
            if (s.useSampleLayout) {
                TemplateSeeder.seedSampleLayout(db, vesselId)
            }
            TemplateSeeder.seedDefaultPermissions(db, vesselId)   // always — the app needs a matrix to function
            _uiState.update { it.copy(isSaving = false, done = true) }
        }
    }
}