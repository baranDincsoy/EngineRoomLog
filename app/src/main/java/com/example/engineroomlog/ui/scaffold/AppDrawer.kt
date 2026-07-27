package com.example.engineroomlog.ui.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    canEditForm: Boolean,
    canManageCrew: Boolean,   // CHIEF only
    canManageFleet: Boolean,  // CHIEF only
    canManagePermissions: Boolean,  // CHIEF only
    onManageGroups: () -> Unit,
    onManageCrew: () -> Unit,
    onSignOut: () -> Unit,
    onJournal: () -> Unit,
    onPdfList: () -> Unit,
    onFleet: () -> Unit,
    onEntry: () -> Unit,
    onPermissions: () -> Unit,
    content: @Composable (Modifier) -> Unit,

) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "EngineRoomLog",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Engine room logbook",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Entry") },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onEntry()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Journal") },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onJournal()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("PDF list") },
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onPdfList()
                    }
                )

                if (canEditForm) {
                    NavigationDrawerItem(
                        label = { Text("Manage groups") },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onManageGroups()
                        }
                    )
                }

                if (canManageCrew) {
                    NavigationDrawerItem(
                        label = { Text("Manage crew") },
                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onManageCrew()
                        }
                    )
                }

                if (canManagePermissions) {
                    NavigationDrawerItem(
                        label = { Text("Permissions") },
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onPermissions()
                        }
                    )
                }

                if (canManageFleet) {
                    NavigationDrawerItem(
                        label = { Text("Fleet connection") },
                        icon = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onFleet()
                        }
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Sign out") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    }
                )

            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    }
}