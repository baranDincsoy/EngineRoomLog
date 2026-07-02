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
import com.example.engineroomlog.ui.theme.EngineRoomLogTheme
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EngineRoomLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VesselSetupScreen(
                        onVesselSaved = { vesselId ->
                            // For now, just log it — navigation comes later
                            Log.d("EngineRoomLog", "Vessel saved with id: $vesselId")
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
