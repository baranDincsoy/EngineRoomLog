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
import com.example.engineroomlog.core.sync.AutoSync
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.entity.CrewMemberEntity
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.entity.VesselProfileEntity
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.OperationalState
import com.example.engineroomlog.ui.login.LoginScreen
import com.example.engineroomlog.ui.navigation.AppNavHost
import com.example.engineroomlog.ui.theme.EngineRoomLogTheme
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AutoSync.start(this, lifecycleScope)
        setContent {
            EngineRoomLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
