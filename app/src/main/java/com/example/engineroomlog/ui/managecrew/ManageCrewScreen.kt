package com.example.engineroomlog.ui.managecrew

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.model.CrewRole

@Composable
fun ManageCrewScreen(
    activeCrewId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageCrewViewModel = viewModel()
) {
    val crew by viewModel.crew.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deactivateTarget by remember { mutableStateOf<CrewMemberEntity?>(null) }

    LaunchedEffect(activeCrewId) { viewModel.activeCrewId = activeCrewId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item(key = "back") {
            TextButton(onClick = onBack) { Text("< Back") }
        }

        items(items = crew, key = { "crew_${it.id}" }) { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = member.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = listOfNotNull(member.rank, member.role.name, "No: ${member.username}")
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (member.id != activeCrewId) {
                    IconButton(onClick = { deactivateTarget = member }) {
                        Icon(Icons.Default.Close, contentDescription = "Deactivate member")
                    }
                }
            }
        }

        item(key = "add_member") {
            TextButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("+ Add crew member") }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var rank by remember { mutableStateOf("") }
        var employeeNo by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(CrewRole.OILER) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false; viewModel.clearError() },
            title = { Text("Add crew member") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Name") }, singleLine = true
                    )
                    OutlinedTextField(
                        value = rank, onValueChange = { rank = it },
                        label = { Text("Rank (optional)") }, singleLine = true
                    )
                    OutlinedTextField(
                        value = employeeNo, onValueChange = { employeeNo = it },
                        label = { Text("Employee no") }, singleLine = true
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        CrewRole.entries.forEachIndexed { index, r ->
                            SegmentedButton(
                                selected = role == r,
                                onClick = { role = r },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index, count = CrewRole.entries.size
                                )
                            ) { Text(r.name) }
                        }
                    }
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCrewMember(name, rank, employeeNo, password, role)
                        showAddDialog = false
                    },
                    enabled = name.isNotBlank() && employeeNo.isNotBlank() && password.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; viewModel.clearError() }) { Text("Cancel") }
            }
        )
    }

    deactivateTarget?.let { member ->
        AlertDialog(
            onDismissRequest = { deactivateTarget = null },
            title = { Text("Deactivate ${member.name}?") },
            text = { Text("They will no longer be able to log in. Their past journal entries are kept.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deactivate(member)
                    deactivateTarget = null
                }) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { deactivateTarget = null }) { Text("Cancel") }
            }
        )
    }
}