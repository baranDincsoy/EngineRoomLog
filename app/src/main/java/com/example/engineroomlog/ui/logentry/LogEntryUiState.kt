package com.example.engineroomlog.ui.logentry

import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.entity.GroupWithParameters




data class LogEntryUiState(
    val groups: List<GroupWithParameters> = emptyList(),
    // Draft values keyed by parameterId — what the user has typed so far
    val draftValues: Map<Long, String> = emptyMap(),
    val isSaving: Boolean = false,
    val savedEntryId: Long? = null,
    val errorMessage: String? = null
)