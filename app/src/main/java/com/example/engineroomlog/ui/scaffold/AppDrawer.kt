package com.example.engineroomlog.ui.scaffold

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onManageGroups: () -> Unit,
    onSignOut: () -> Unit,
    onJournal: () -> Unit,
    onEntry: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "EngineRoomLog",
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Entry") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onEntry()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Journal") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onJournal()
                    }
                )

                if (canEditForm) {
                    NavigationDrawerItem(
                        label = { Text("Manage groups") },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onManageGroups()
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