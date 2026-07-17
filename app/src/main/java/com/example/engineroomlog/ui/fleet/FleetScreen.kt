package com.example.engineroomlog.ui.fleet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.engineroomlog.core.sync.FleetConnection
import com.example.engineroomlog.core.sync.JournalUploader
import kotlinx.coroutines.launch

@Composable
fun FleetScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var connected by remember { mutableStateOf(FleetConnection.isConnected) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var syncMsg by remember { mutableStateOf<String?>(null) }
    var syncing by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("< Back") }

        if (connected) {
            Text("Connected to fleet", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Fleet ID: ${FleetConnection.fleetId ?: "—"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Exported journals will be uploaded to the company space when online.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    syncing = true; syncMsg = null
                    scope.launch {
                        val report = JournalUploader.uploadPending(context)
                        syncing = false
                        syncMsg = when {
                            report.failed -> "Could not reach the fleet space — check network"
                            report.uploaded == 0 && report.pending == 0 -> "All journals already uploaded"
                            else -> "Uploaded ${report.uploaded} journal(s)" +
                                    if (report.pending > 0) ", ${report.pending} pending" else ""
                        }
                    }
                },
                enabled = !syncing,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (syncing) "Uploading…" else "Sync now") }

            if (syncMsg != null) {
                Text(syncMsg!!, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    FleetConnection.disconnect()
                    connected = false
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Disconnect") }
        } else {
            Text("Connect to fleet", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Enter the fleet account provided by your company. " +
                        "This links the device, not a person — crew log in as usual.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Fleet account") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    busy = true; error = null
                    scope.launch {
                        val result = FleetConnection.connect(email, password)
                        busy = false
                        if (result.isSuccess) connected = true
                        else error = "Connection failed — check the account details and network"
                    }
                },
                enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (busy) "Connecting…" else "Connect") }
        }
    }
}