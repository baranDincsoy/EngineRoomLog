package com.example.engineroomlog.ui.managegroups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.model.OperationalState

@Composable
fun ManageGroupsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageGroupsViewModel = viewModel()
) {
    val groups by viewModel.groups.collectAsState()

    var renameTarget by remember { mutableStateOf<ParameterGroupEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ParameterGroupEntity?>(null) }
    var moveTarget by remember { mutableStateOf<ParameterEntity?>(null) }
    var editTarget by remember { mutableStateOf<ParameterEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item(key = "back") {
            TextButton(onClick = onBack) { Text("< Back") }
        }

        groups.forEach { gwp ->
            item(key = "group_${gwp.group.id}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gwp.group.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { renameTarget = gwp.group }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename group")
                    }
                    IconButton(onClick = { deleteTarget = gwp.group }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove group")
                    }
                }
            }

            items(
                items = gwp.parameters,
                key = { "param_${it.id}" }
            ) { parameter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = parameter.name,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { moveTarget = parameter }) {
                        Text("Move")
                    }
                    TextButton(onClick = { editTarget = parameter }) {
                        Text("Edit")
                    }
                }
            }
        }
    }

    // --- Rename dialog ---
    renameTarget?.let { group ->
        var newName by remember(group.id) { mutableStateOf(group.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename group") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameGroup(group, newName)
                        renameTarget = null
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    // --- Delete confirmation ---
    deleteTarget?.let { group ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Remove group?") },
            text = { Text("\"${group.name}\" and its parameters will no longer appear on the form. Past records are kept.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deactivateGroup(group)
                        deleteTarget = null
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    // --- Move dialog: pick a target group ---
    moveTarget?.let { parameter ->
        AlertDialog(
            onDismissRequest = { moveTarget = null },
            title = { Text("Move \"${parameter.name}\" to…") },
            text = {
                LazyColumn {
                    items(
                        items = groups.map { it.group }
                            .filter { it.id != parameter.groupId },
                        key = { "target_${it.id}" }
                    ) { target ->
                        TextButton(
                            onClick = {
                                viewModel.moveParameter(parameter, target.id)
                                moveTarget = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(target.name) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { moveTarget = null }) { Text("Cancel") }
            }
        )
    }
    // --- Edit parameter dialog ---
    editTarget?.let { parameter ->
        var editName by remember(parameter.id) { mutableStateOf(parameter.name) }
        var editUnit by remember(parameter.id) { mutableStateOf(parameter.unit ?: "") }
        var editState by remember(parameter.id) { mutableStateOf(parameter.state) }

        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Edit parameter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editUnit,
                        onValueChange = { editUnit = it },
                        label = { Text("Unit (optional)") },
                        singleLine = true
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        OperationalState.entries.forEachIndexed { index, state ->
                            SegmentedButton(
                                selected = editState == state,
                                onClick = { editState = state },
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
                        viewModel.updateParameter(parameter, editName, editUnit, editState)
                        editTarget = null
                    },
                    enabled = editName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) { Text("Cancel") }
            }
        )
    }
}