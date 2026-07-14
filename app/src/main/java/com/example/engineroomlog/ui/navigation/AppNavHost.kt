package com.example.engineroomlog.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.engineroomlog.ui.chiefsetup.ChiefSetupScreen
import com.example.engineroomlog.ui.journal.JournalScreen
import com.example.engineroomlog.ui.logentry.LogEntryScreen
import com.example.engineroomlog.ui.login.LoginScreen
import com.example.engineroomlog.ui.managecrew.ManageCrewScreen
import com.example.engineroomlog.ui.managegroups.ManageGroupsScreen
import com.example.engineroomlog.ui.pdflist.PdfListScreen
import com.example.engineroomlog.ui.scaffold.AppScaffold
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen
import kotlinx.coroutines.flow.first

// Route names live in one place so we never mistype them
object Routes {
    const val VESSEL_SETUP = "vessel_setup"
    const val LOGIN = "login"
    const val HOME = "home/{crewId}/{role}"
    const val MANAGE_GROUPS = "manage_groups"
    const val JOURNAL = "journal/{crewId}/{role}"
    const val MANAGE_CREW = "manage_crew"
    const val PDF_LIST = "pdf_list"
    fun journalWith(crewId: Long, role: String) = "journal/$crewId/$role"
    fun homeWith(crewId: Long, role: String) = "home/$crewId/$role"

    const val CHIEF_SETUP = "chief_setup/{vesselId}"
    fun chiefSetupWith(vesselId: Long) = "chief_setup/$vesselId"

}



@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val context = androidx.compose.ui.platform.LocalContext.current
    var startDestination by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    LaunchedEffect(Unit) {
        val hasVessel = com.example.engineroomlog.data.local.database.DatabaseProvider
            .getDatabase(context)
            .vesselProfileDao().getActiveVessels()
            .first().isNotEmpty()
        startDestination = if (hasVessel) Routes.LOGIN else Routes.VESSEL_SETUP
    }
    if (startDestination == null) return   // one-frame blank while the check runs

    var currentCrewId by rememberSaveable { mutableStateOf(0L) }

    // Which route is on screen right now?
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Role travels with the HOME route args; remember it after login
    var currentRole by rememberSaveable { mutableStateOf("OILER") }
    val canEdit = currentRole == "ENGINEER" || currentRole == "CHIEF"
    val canManageCrew = currentRole == "CHIEF"

    val isLoggedInArea = currentRoute != null &&
            currentRoute != Routes.LOGIN &&
            currentRoute != Routes.VESSEL_SETUP &&
            currentRoute != Routes.CHIEF_SETUP

    if (isLoggedInArea) {
        AppScaffold(
            title = when (currentRoute) {
                Routes.JOURNAL -> "Journal"
                Routes.MANAGE_GROUPS -> "Manage groups"
                Routes.MANAGE_CREW -> "Manage crew"
                else -> "Engine Log"
            },
            canEditForm = canEdit,
            canManageCrew = canManageCrew,
            onManageGroups = { navController.navigate(Routes.MANAGE_GROUPS) },
            onManageCrew = { navController.navigate(Routes.MANAGE_CREW) },
            onJournal = { navController.navigate(Routes.journalWith(currentCrewId, currentRole)) },
            onPdfList = { navController.navigate(Routes.PDF_LIST) },
            onEntry = { navController.popBackStack(Routes.HOME, inclusive = false) },
            onSignOut = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) { paddingModifier ->
            AppNavGraph(navController, paddingModifier, currentCrewId) { crewId, role -> currentCrewId = crewId; currentRole = role }
        }
    } else {
        AppNavGraph(navController, modifier, currentCrewId) { crewId, role -> currentCrewId = crewId; currentRole = role }
    }
}

@Composable
private fun AppNavGraph(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
    currentCrewId : Long,
    onLoginResolved: (Long, String) -> Unit,

) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.VESSEL_SETUP) { /* mevcut hali */ }

        composable(Routes.MANAGE_CREW) {
            ManageCrewScreen(
                activeCrewId = currentCrewId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { crewId, role ->
                    onLoginResolved(crewId, role)
                    navController.navigate(Routes.homeWith(crewId, role)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MANAGE_GROUPS) {
            ManageGroupsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.JOURNAL,
            arguments = listOf(
                navArgument("crewId") { type = NavType.LongType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            JournalScreen(
                crewId = backStackEntry.arguments?.getLong("crewId") ?: 0L,
                role = backStackEntry.arguments?.getString("role") ?: "OILER"
            )
        }

        composable(Routes.PDF_LIST) {
            PdfListScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.HOME,
            arguments = listOf(
                navArgument("crewId") { type = NavType.LongType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val crewId = backStackEntry.arguments?.getLong("crewId") ?: 0L
            val role = backStackEntry.arguments?.getString("role") ?: "OILER"
            LogEntryScreen(
                crewId = crewId,
                role = role,
                onExitConfirmed = { navController.popBackStack() }
            )
        }

        composable(Routes.VESSEL_SETUP) {
            VesselSetupScreen(
                onVesselSaved = { vesselId ->
                    navController.navigate(Routes.chiefSetupWith(vesselId))
                }
            )
        }

        composable(
            route = Routes.CHIEF_SETUP,
            arguments = listOf(navArgument("vesselId") { type = NavType.LongType })
        ) { backStackEntry ->
            ChiefSetupScreen(
                vesselId = backStackEntry.arguments?.getLong("vesselId") ?: 0L,
                onSetupComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }   // setup is one-way; no back into it
                    }
                }
            )
        }
    }
}