package com.example.engineroomlog.ui.logentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.model.OperationalState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParameterDialog(
    groups: List<ParameterGroupEntity>,
    lastCreatedGroupId: Long?,
    onConfirm: (groupId: Long, name: String, unit: String?, state: OperationalState) -> Unit,
    onCreateGroup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(groups.firstOrNull()) }
    var selectedState by remember { mutableStateOf(OperationalState.AT_SEA) }
    var groupMenuExpanded by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    // When a new group is created, auto-select it in the dropdown
    LaunchedEffect(lastCreatedGroupId, groups) {
        if (lastCreatedGroupId != null) {
            groups.firstOrNull { it.id == lastCreatedGroupId }
                ?.let { selectedGroup = it }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add parameter") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // TEMPORARY DEBUG — remove later
                Text("DEBUG v2: ${groups.size} groups: ${groups.joinToString { it.name }}")
                ExposedDropdownMenuBox(
                    expanded = groupMenuExpanded,
                    onExpandedChange = { groupMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedGroup?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupMenuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = groupMenuExpanded,
                        onDismissRequest = { groupMenuExpanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroup = group
                                    groupMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ New group…") },
                            onClick = {
                                groupMenuExpanded = false
                                showNewGroupDialog = true
                            }
                        )
                    }
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    OperationalState.entries.forEachIndexed { index, state ->
                        SegmentedButton(
                            selected = selectedState == state,
                            onClick = { selectedState = state },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = OperationalState.entries.size
                            )
                        ) { Text(state.name.replace("_", " ")) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val group = selectedGroup ?: return@Button
                    onConfirm(group.id, name, unit.ifBlank { null }, selectedState)
                },
                enabled = name.isNotBlank() && selectedGroup != null
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
    if (showNewGroupDialog) {
        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            title = { Text("New group") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Group name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateGroup(newGroupName)      // ← önce bu çalışır
                        newGroupName = ""
                        showNewGroupDialog = false       // ← pencereyi bu kapatır
                    },
                    enabled = newGroupName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) { Text("Cancel") }
            }
        )
    }
}