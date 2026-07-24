package com.example.engineroomlog.ui.managecrew

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.core.security.PasswordHasher
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.model.Ranks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ManageCrewViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val crewDao = db.crewMemberDao()

    private var activeVesselId: Long = 1L

    private val _crew = MutableStateFlow<List<CrewMemberEntity>>(emptyList())
    val crew: StateFlow<List<CrewMemberEntity>> = _crew.asStateFlow()

    // Set by the screen so the chief cannot deactivate their own account
    var activeCrewId: Long = 0L

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            val vessel = db.vesselProfileDao().getActiveVessels().first().firstOrNull()
            if (vessel != null) activeVesselId = vessel.id

            crewDao.getActiveCrew(activeVesselId).collect { list ->
                _crew.value = list
            }
        }
    }

    fun addCrewMember(
        name: String,
        rank: String,
        employeeNo: String,
        password: String
    ) {
        val trimmedName = name.trim()
        val trimmedNo = employeeNo.trim()
        val trimmedRank = rank.trim()
        if (trimmedName.isEmpty() || trimmedNo.isEmpty() || password.isEmpty() || trimmedRank.isEmpty()) return

        viewModelScope.launch {
            if (crewDao.findByUsername(trimmedNo) != null) {
                _errorMessage.value = "Employee no $trimmedNo is already in use"
                return@launch
            }
            crewDao.insert(
                CrewMemberEntity(
                    vesselProfileId = activeVesselId,
                    name = trimmedName,
                    rank = trimmedRank,
                    username = trimmedNo,
                    passwordHash = PasswordHasher.hash(password)
                )
            )
            _errorMessage.value = null
        }
    }

    fun deactivate(member: CrewMemberEntity) {
        if (member.id == activeCrewId) return   // safety net; UI hides the button too
        viewModelScope.launch {
            crewDao.update(member.copy(isActive = false))
        }
    }

    fun clearError() { _errorMessage.value = null }

    fun resetPassword(member: CrewMemberEntity, newPassword: String) {
        if (newPassword.isBlank()) return
        viewModelScope.launch {
            crewDao.update(member.copy(passwordHash = PasswordHasher.hash(newPassword)))
            _errorMessage.value = null
        }
    }




}