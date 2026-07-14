package com.example.engineroomlog.ui.chiefsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChiefSetupScreen(
    vesselId: Long,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChiefSetupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.done) {
        if (uiState.done) onSetupComplete()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create the chief engineer account", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This account manages the crew and the form on this tablet.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.employeeNo,
            onValueChange = { viewModel.onEmployeeNoChange(it) },
            label = { Text("Employee no") },
            supportingText = { Text("Used to log in") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Form layout", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = uiState.useSampleLayout,
                onClick = { viewModel.onTemplateChange(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Sample layout") }
            SegmentedButton(
                selected = !uiState.useSampleLayout,
                onClick = { viewModel.onTemplateChange(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Start empty") }
        }

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { viewModel.finishSetup(vesselId) },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (uiState.isSaving) "Saving…" else "Finish setup") }
    }
}