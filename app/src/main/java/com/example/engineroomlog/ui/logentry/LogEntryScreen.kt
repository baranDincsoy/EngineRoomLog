package com.example.engineroomlog.ui.logentry

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

@Composable
fun LogEntryScreen(
    crewId: Long,
    modifier: Modifier = Modifier,
    role: String,
    viewModel: LogEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val canEditForm = role == "ENGINEER" || role == "CHIEF"
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

        items(
            items = uiState.todaysEntries,
            key = { "entry_${it.id}" }
        ) { entry ->
            val time = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(entry.timestamp))
            Text(
                text = "• $time — ${entry.collectedByName} (#${entry.collectedByCrewId})",
                style = MaterialTheme.typography.bodySmall
            )
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

        // --- Save button: once, AFTER the groups ---
        item(key = "save_button") {
            Button(
                onClick = { viewModel.saveEntry() },
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