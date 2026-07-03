package com.example.engineroomlog.ui.login

import com.example.engineroomlog.data.local.model.CrewRole

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Set when login succeeds; carries who logged in
    val loggedInCrewId: Long? = null,
    val loggedInRole: CrewRole? = null
)