package com.example.engineroomlog.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engineroomlog.data.local.model.Permission
import com.example.engineroomlog.data.local.model.Ranks

private val LABEL_COL = 150.dp
private val CELL = 64.dp

@Composable
fun PermissionMatrixScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionMatrixViewModel = viewModel()
) {
    val saved by viewModel.saved.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val hScroll = rememberScrollState()
    var confirm by remember { mutableStateOf<Cell?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        TextButton(onClick = onBack) { Text("< Back") }
        Text(
            "Tap a cell to grant or revoke. Green = newly granted, red = revoked, blue = saved. Then Save changes.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // Header row: empty corner + rank names
            Row {
                Box(Modifier.width(LABEL_COL).height(CELL))
                Row(Modifier.horizontalScroll(hScroll)) {
                    Ranks.ALL.forEach { rank ->
                        Box(
                            Modifier.width(CELL).height(CELL).padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                rank.split(" ").joinToString("\n"),  // stack words to fit
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // One row per permission
            Permission.entries.forEach { perm ->
                Row {
                    Box(
                        Modifier.width(LABEL_COL).height(CELL).padding(4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            perm.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(Modifier.horizontalScroll(hScroll)) {
                        Ranks.ALL.forEach { rank ->
                            val cell = Cell(rank, perm)
                            val inDraft = cell in draft
                            val inSaved = cell in saved
                            val color = when {
                                inDraft && !inSaved -> Color(0xFF4CAF50)   // newly granted — green
                                !inDraft && inSaved -> Color(0xFFE57373)   // revoked — red
                                inDraft && inSaved -> MaterialTheme.colorScheme.primary  // saved — blue
                                else -> MaterialTheme.colorScheme.surfaceVariant         // none
                            }
                            Box(
                                Modifier
                                    .width(CELL).height(CELL).padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                                    .clickable { confirm = cell },
                                contentAlignment = Alignment.Center
                            ) {
                                if (inDraft) Text("✓", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            TextButton(
                onClick = { viewModel.discard() },
                enabled = viewModel.hasChanges
            ) { Text("Discard") }
            Button(
                onClick = { viewModel.save() },
                enabled = viewModel.hasChanges,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) { Text("Save changes") }
        }
    }

    confirm?.let { cell ->
        val granting = cell !in draft
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(if (granting) "Grant permission?" else "Revoke permission?") },
            text = {
                Text(
                    "${if (granting) "Give" else "Remove from"} ${cell.rank}: " +
                            cell.permission.name.replace("_", " ")
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.toggle(cell); confirm = null }) {
                    Text(if (granting) "Grant" else "Revoke")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = null }) { Text("Cancel") }
            }
        )
    }
}