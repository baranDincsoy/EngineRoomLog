package com.example.engineroomlog.ui.login

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Set when login succeeds; carries who logged in (rank drives permissions)
    val loggedInCrewId: Long? = null,
    val loggedInRank: String? = null
)