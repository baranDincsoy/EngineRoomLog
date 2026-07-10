package com.example.engineroomlog.ui.journal

import com.example.engineroomlog.data.local.entity.GroupWithParameters

// One journal row: a timestamp plus its values keyed by parameterId
data class JournalRow(
    val entryId: Long,
    val timestamp: Long,
    val collectedByName: String,
    val collectedByCrewId: Long?,
    val remarks: String?,
    val values: Map<Long, String>
)

data class JournalUiState(
    val dayStartMillis: Long = 0L,
    val groups: List<GroupWithParameters> = emptyList(),
    val rows: List<JournalRow> = emptyList()
)