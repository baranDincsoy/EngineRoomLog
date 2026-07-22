package com.example.engineroomlog.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineroomlog.core.security.PasswordHasher
import com.example.engineroomlog.data.local.database.DatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val crewDao =
        DatabaseProvider.getDatabase(application).crewMemberDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun login() {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password

        if (username.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter username and password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val member = crewDao.findByUsername(username)

            // Same generic message whether the user is missing or the password is wrong
            if (member == null || !PasswordHasher.verify(password, member.passwordHash)) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Invalid username or password")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loggedInCrewId = member.id,
                    loggedInRank = member.rank
                )
            }
        }
    }
}