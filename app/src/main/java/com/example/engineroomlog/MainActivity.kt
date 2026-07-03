package com.example.engineroomlog

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.engineroomlog.core.security.PasswordHasher
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.model.CrewRole
import com.example.engineroomlog.ui.login.LoginScreen
import com.example.engineroomlog.ui.navigation.AppNavHost
import com.example.engineroomlog.ui.theme.EngineRoomLogTheme
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
// TEMPORARY: seed a test admin so we can test login. Remove later.
        lifecycleScope.launch {
            val crewDao = DatabaseProvider.getDatabase(applicationContext).crewMemberDao()
            if (crewDao.findByUsername("admin") == null) {
                crewDao.insert(
                    CrewMemberEntity(
                        vesselProfileId = 1,
                        name = "Test Admin",
                        rank = "Chief Engineer",
                        role = CrewRole.CHIEF,
                        username = "admin",
                        passwordHash = PasswordHasher.hash("1234")
                    )
                )
            }
        }

        setContent {
            EngineRoomLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
