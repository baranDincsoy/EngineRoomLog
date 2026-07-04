package com.example.engineroomlog.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.engineroomlog.ui.logentry.LogEntryScreen
import com.example.engineroomlog.ui.login.LoginScreen
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen

// Route names live in one place so we never mistype them
object Routes {
    const val VESSEL_SETUP = "vessel_setup"
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.VESSEL_SETUP) {
            VesselSetupScreen(
                onVesselSaved = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { crewId, role ->
                    navController.navigate(Routes.HOME) {
                        // Remove login from the back stack:
                        // back button must not return to the login screen
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            LogEntryScreen()
        }
    }
}