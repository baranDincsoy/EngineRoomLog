package com.example.engineroomlog.ui.logentry

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.model.OperationalState
import com.example.engineroomlog.data.local.model.Ranks

@Composable
fun LogEntryScreen(
    crewId: Long,
    modifier: Modifier = Modifier,
    rank: String,
    onExitConfirmed: () -> Unit,
    viewModel: LogEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val canEditForm = rank in listOf(
        Ranks.CHIEF_ENGINEER, Ranks.SECOND_ENGINEER, Ranks.THIRD_ENGINEER,
        Ranks.FOURTH_ENGINEER, Ranks.ELECTRICAL_OFFICER
    )
    var showSaveConfirm by remember { mutableStateOf(false) }

    var showExitWarning by remember { mutableStateOf(false) }
    val hasUnsavedValues = uiState.draftValues.any { it.value.isNotBlank() } ||
            uiState.draftRemarks.isNotBlank()

    BackHandler(enabled = hasUnsavedValues) {
        showExitWarning = true
    }

    // Tell the ViewModel who is logged in (once per crewId)
    LaunchedEffect(crewId) {
        viewModel.setActiveCrew(crewId)
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "state_selector") {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                SegmentedButton(
                    selected = uiState.selectedState == OperationalState.AT_SEA,
                    onClick = { viewModel.onStateSelected(OperationalState.AT_SEA) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("At Sea") }

                SegmentedButton(
                    selected = uiState.selectedState == OperationalState.IN_PORT,
                    onClick = { viewModel.onStateSelected(OperationalState.IN_PORT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("In Port") }
            }
        }
        // --- Groups and their parameters ---
        uiState.visibleGroups.forEach { groupWithParams ->

            item(key = "group_${groupWithParams.group.id}") {
                Text(
                    text = groupWithParams.group.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            items(
                items = groupWithParams.parameters,
                key = { "param_${it.id}" }
            ) { parameter ->
                ParameterRow(
                    parameter = parameter,
                    value = uiState.draftValues[parameter.id] ?: "",
                    onValueChange = { viewModel.onValueChange(parameter.id, it) },
                    onRemove = { viewModel.deactivateParameter(parameter) },
                    canEdit = canEditForm
                )
            }
        }   // forEach ends
        if (canEditForm) {
            item(key = "add_parameter") {
                TextButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("+ Add parameter") }
            }
        }
        item(key = "remarks") {
            OutlinedTextField(
                value = uiState.draftRemarks,
                onValueChange = { viewModel.onRemarksChange(it) },
                label = { Text("Remarks (optional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
        // --- Save button: once, AFTER the groups ---
        item(key = "save_button") {
            Button(
                onClick = { showSaveConfirm = true },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(if (uiState.isSaving) "Saving…" else "Save entry")
            }
        }
    }   // LazyColumn ends
    if (showAddDialog) {
        AddParameterDialog(
            groups = uiState.groups.map { it.group },
            lastCreatedGroupId = uiState.lastCreatedGroupId,
            onConfirm = { groupId, name, unit, state ->
                viewModel.addParameter(groupId, name, unit, state)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
            onCreateGroup = { groupName ->
                android.util.Log.d("EngineRoomLog", "onCreateGroup called: $groupName")
                viewModel.addGroup(groupName)

            },
        )
    }
    if (showSaveConfirm) {
        val filledCount = uiState.draftValues.count { it.value.isNotBlank() }
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("Submit entry?") },
            text = { Text("$filledCount value(s) will be saved to the log.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveEntry()
                        showSaveConfirm = false
                    }
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) { Text("Cancel") }
            }
        )
    }
    if (showExitWarning) {
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            title = { Text("Unsaved values") },
            text = { Text("You have entered values that are not submitted. Leave anyway?") },
            confirmButton = {
                Button(onClick = {
                    showExitWarning = false
                    onExitConfirmed()
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showExitWarning = false }) { Text("Stay") }
            }
        )
    }
}

@Composable
private fun ParameterRow(
    parameter: ParameterEntity,
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    canEdit : Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = parameter.name,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = parameter.unit ?: "",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .width(40.dp)
                .padding(start = 6.dp)
        )
        if (canEdit) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove parameter")
            }
        }
    }
}