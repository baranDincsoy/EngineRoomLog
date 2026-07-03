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
import com.example.engineroomlog.data.local.entity.ParameterEntity
import com.example.engineroomlog.data.local.entity.ParameterGroupEntity
import com.example.engineroomlog.data.local.model.Cadence
import com.example.engineroomlog.data.local.model.CrewRole
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
// TEMPORARY: seed test data so we can develop the entry screen. Remove later.
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(applicationContext)
            val crewDao = db.crewMemberDao()
            val groupDao = db.parameterGroupDao()
            val paramDao = db.parameterDao()

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

            // Seed a couple of groups + parameters once
            if (groupDao.getGroupsForVessel(1).first().isEmpty()) {
                val mainEngineId = groupDao.insert(
                    ParameterGroupEntity(
                        vesselProfileId = 1,
                        name = "Main Engine",
                        displayOrder = 0
                    )
                )
                val generatorId = groupDao.insert(
                    ParameterGroupEntity(vesselProfileId = 1, name = "Generators", displayOrder = 1)
                )

                paramDao.insert(
                    ParameterEntity(
                        groupId = mainEngineId, name = "RPM", unit = "rpm",
                        state = OperationalState.AT_SEA, cadence = Cadence.HOURLY,
                        displayOrder = 0, isDefault = true
                    )
                )
                paramDao.insert(
                    ParameterEntity(
                        groupId = mainEngineId, name = "L.O. Pressure", unit = "bar",
                        state = OperationalState.AT_SEA, cadence = Cadence.HOURLY,
                        displayOrder = 1, isDefault = true
                    )
                )
                paramDao.insert(
                    ParameterEntity(
                        groupId = mainEngineId, name = "Jacket CW Outlet", unit = "°C",
                        state = OperationalState.AT_SEA, cadence = Cadence.HOURLY,
                        displayOrder = 2, isDefault = true
                    )
                )
                paramDao.insert(
                    ParameterEntity(
                        groupId = generatorId, name = "No.1 Gen Voltage", unit = "V",
                        state = OperationalState.BOTH, cadence = Cadence.HOURLY,
                        displayOrder = 0, isDefault = true
                    )
                )
                paramDao.insert(
                    ParameterEntity(
                        groupId = generatorId, name = "No.1 Gen Load", unit = "kW",
                        state = OperationalState.BOTH, cadence = Cadence.HOURLY,
                        displayOrder = 1, isDefault = true
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
