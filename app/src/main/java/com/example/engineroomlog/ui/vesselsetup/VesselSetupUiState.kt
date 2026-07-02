package com.example.engineroomlog.ui.vesselsetup

data class VesselSetupUiState(
    val name: String = "",
    val imoNumber: String = "",
    val isSaving: Boolean = false,
    val savedVesselId: Long? = null,
    val errorMessage: String? = null
)