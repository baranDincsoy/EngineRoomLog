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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engineroomlog.data.local.entity.ParameterEntity

@Composable
fun LogEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: LogEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Today's entries: once, BEFORE the groups ---
        item(key = "today_header") {
            Text(
                text = "Today's entries: ${uiState.todaysEntries.size}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(
            items = uiState.todaysEntries,
            key = { "entry_${it.id}" }
        ) { entry ->
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(entry.timestamp))
            Text(
                text = "• $time — ${entry.collectedByName} (${entry.status})",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // --- Groups and their parameters ---
        uiState.groups.forEach { groupWithParams ->

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
                    onValueChange = { viewModel.onValueChange(parameter.id, it) }
                )
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
    }
}

@Composable
private fun ParameterRow(
    parameter: ParameterEntity,
    value: String,
    onValueChange: (String) -> Unit
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
    }
}