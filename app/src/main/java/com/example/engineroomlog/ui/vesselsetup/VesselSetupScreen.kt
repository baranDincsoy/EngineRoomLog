package com.example.engineroomlog.ui.vesselsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VesselSetupScreen(
    onVesselSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VesselSetupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // React to a successful save: navigate away once we have an id
    if (uiState.savedVesselId != null) {
        // Navigate exactly once when the vessel is saved
        LaunchedEffect(uiState.savedVesselId) {
            val id = uiState.savedVesselId
            if (id != null) {
                onVesselSaved(id)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add your vessel")

        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Vessel name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.imoNumber,
            onValueChange = { viewModel.onImoChange(it) },
            label = { Text("IMO number (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage!!)
        }

        Button(
            onClick = { viewModel.saveVessel() },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Save vessel")
            }
        }
    }
}