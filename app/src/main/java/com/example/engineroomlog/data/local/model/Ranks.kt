package com.example.engineroomlog.data.local.model

// The engine department ranks. Single source of truth for dropdowns and the
// permission matrix columns. Order matters: seniority top to bottom.
object Ranks {
    const val CHIEF_ENGINEER = "Chief Engineer"
    const val SECOND_ENGINEER = "Second Engineer"
    const val THIRD_ENGINEER = "Third Engineer"
    const val FOURTH_ENGINEER = "Fourth Engineer"
    const val ELECTRICAL_OFFICER = "Electrical Officer"
    const val FITTER = "Fitter"
    const val MOTORMAN = "Motorman"
    const val OILER = "Oiler"
    const val WIPER = "Wiper"

    val ALL = listOf(
        CHIEF_ENGINEER, SECOND_ENGINEER, THIRD_ENGINEER, FOURTH_ENGINEER,
        ELECTRICAL_OFFICER, FITTER, MOTORMAN, OILER, WIPER
    )
}