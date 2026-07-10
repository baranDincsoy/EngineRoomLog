package com.example.engineroomlog.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private val TIME_COL_WIDTH = 72.dp
private val VALUE_COL_WIDTH = 84.dp

@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ONE horizontal scroll state shared by header and every row
    val hScroll = rememberScrollState()

    var detailRow by remember { mutableStateOf<JournalRow?>(null) }

    val flatParams = uiState.groups.flatMap { it.parameters }
    val dayLabel = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        .format(Date(uiState.dayStartMillis))

    Column(modifier = modifier.fillMaxSize()) {

        // Day navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.goToPreviousDay() }) { Text("<") }
            Text(
                text = dayLabel,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { viewModel.goToNextDay() }) { Text(">") }
        }

        // Header row: time column + parameter names
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            Text(
                text = "Time",
                modifier = Modifier.width(TIME_COL_WIDTH).padding(4.dp),
                style = MaterialTheme.typography.labelMedium
            )
            flatParams.forEach { param ->
                Column(modifier = Modifier.width(VALUE_COL_WIDTH).padding(4.dp)) {
                    Text(
                        text = param.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2
                    )
                    Text(
                        text = param.unit ?: "",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Data rows
        LazyColumn {
            items(
                items = uiState.rows,
                key = { "row_${it.entryId}" }
            ) { row ->
                Row(modifier = Modifier
                    .clickable {detailRow = row}
                    .horizontalScroll(hScroll)) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(row.timestamp)),
                        modifier = Modifier.width(TIME_COL_WIDTH).padding(4.dp)
                    )
                    flatParams.forEach { param ->
                        Text(
                            text = row.values[param.id] ?: "—",
                            modifier = Modifier.width(VALUE_COL_WIDTH).padding(4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    detailRow?.let { row ->
        AlertDialog(
            onDismissRequest = { detailRow = null },
            title = {
                Text(
                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(Date(row.timestamp))
                )
            },
            text = {
                Column {
                    // Who + when: the accountability line
                    Text(
                        text = "Collected by: ${row.collectedByName}" +
                                (row.collectedByCrewId?.let { " (ID: $it)" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Remarks: ${row.remarks ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    // Readings: only filled values, name = value unit
                    flatParams
                        .filter { row.values.containsKey(it.id) }
                        .forEach { param ->
                            Text(
                                text = "${param.name}: ${row.values[param.id]}" +
                                        (param.unit?.let { " $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailRow = null }) { Text("Close") }
            }
        )
    }
}