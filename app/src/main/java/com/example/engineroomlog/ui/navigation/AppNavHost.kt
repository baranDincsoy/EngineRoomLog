package com.example.engineroomlog.ui.navigation

import android.R.attr.type
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.engineroomlog.ui.logentry.LogEntryScreen
import com.example.engineroomlog.ui.login.LoginScreen
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen

// Route names live in one place so we never mistype them
object Routes {
    const val VESSEL_SETUP = "vessel_setup"
    const val LOGIN = "login"
    const val HOME = "home/{crewId}"

    fun homeWith(crewId: Long) = "home/$crewId"
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
                    navController.navigate(Routes.homeWith(crewId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            arguments = listOf(navArgument("crewId") { type = NavType.LongType })
        ) { backStackEntry ->
            val crewId = backStackEntry.arguments?.getLong("crewId") ?: 0L
            LogEntryScreen(crewId = crewId)
        }
    }
}