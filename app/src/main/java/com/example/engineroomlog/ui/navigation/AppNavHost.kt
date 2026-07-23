package com.example.engineroomlog.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.example.engineroomlog.data.local.database.TemplateSeeder
import com.example.engineroomlog.data.local.entity.RankPermissionEntity
import com.example.engineroomlog.data.local.model.Permission
import com.example.engineroomlog.data.local.model.Ranks
import com.example.engineroomlog.ui.chiefsetup.ChiefSetupScreen
import com.example.engineroomlog.ui.fleet.FleetScreen
import com.example.engineroomlog.ui.journal.JournalScreen
import com.example.engineroomlog.ui.logentry.LogEntryScreen
import com.example.engineroomlog.ui.login.LoginScreen
import com.example.engineroomlog.ui.managecrew.ManageCrewScreen
import com.example.engineroomlog.ui.managegroups.ManageGroupsScreen
import com.example.engineroomlog.ui.pdflist.PdfListScreen
import com.example.engineroomlog.ui.permissions.PermissionMatrixScreen
import com.example.engineroomlog.ui.scaffold.AppScaffold
import com.example.engineroomlog.ui.vesselsetup.VesselSetupScreen
import kotlinx.coroutines.flow.first

// Route names live in one place so we never mistype them
object Routes {
    const val VESSEL_SETUP = "vessel_setup"
    const val LOGIN = "login"
    const val HOME = "home/{crewId}/{rank}"
    const val MANAGE_GROUPS = "manage_groups"

    const val JOURNAL = "journal/{crewId}/{rank}"
    const val MANAGE_CREW = "manage_crew"
    const val PDF_LIST = "pdf_list"
    fun journalWith(crewId: Long, rank: String) = "journal/$crewId/$rank"
    fun homeWith(crewId: Long, rank: String) = "home/$crewId/$rank"

    const val CHIEF_SETUP = "chief_setup/{vesselId}"
    fun chiefSetupWith(vesselId: Long) = "chief_setup/$vesselId"

    const val FLEET = "fleet"
    const val PERMISSIONS = "permissions"





}



@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    var startDestination by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    LaunchedEffect(Unit) {
        val database = DatabaseProvider.getDatabase(context)
        val vessels = database.vesselProfileDao().getActiveVessels().first()
        val hasVessel = vessels.isNotEmpty()

        // Backfill permission matrix for vessels created before the matrix existed
        vessels.forEach { v ->
            val dao = database.rankPermissionDao()
            val existing = dao.getMatrix(v.id).first()
            if (existing.isEmpty()) {
                TemplateSeeder.seedDefaultPermissions(database, v.id)
            } else {
                // Chief must always be able to manage the matrix — otherwise nobody can
                dao.grant(
                    RankPermissionEntity(
                        vesselProfileId = v.id,
                        rank = Ranks.CHIEF_ENGINEER,
                        permission = Permission.MANAGE_PERMISSIONS
                    )
                )
            }
        }
        startDestination = if (hasVessel) Routes.LOGIN else Routes.VESSEL_SETUP
    }
    if (startDestination == null) return   // one-frame blank while the check runs

    var currentCrewId by rememberSaveable { mutableStateOf(0L) }

    // Which route is on screen right now?
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route



    var currentRank by rememberSaveable { mutableStateOf("") }
    var currentPermissions by remember { mutableStateOf(emptySet<Permission>()) }


    // Resolve this rank's permissions from the vessel's matrix, once per rank change
    LaunchedEffect(currentRank) {
        if (currentRank.isEmpty()) {
            currentPermissions = emptySet()
        } else {
            val database = DatabaseProvider.getDatabase(context)
            val vessel = database.vesselProfileDao().getActiveVessels().first().firstOrNull()
            currentPermissions = if (vessel == null) emptySet()
            else database.rankPermissionDao()
                .getPermissionsForRank(vessel.id, currentRank).toSet()
        }
    }

    // Role travels with the HOME route args; remember it after login
    val canEdit = Permission.EDIT_FORM in currentPermissions
    val canManageCrew = Permission.MANAGE_CREW in currentPermissions
    val canManageFleet = Permission.MANAGE_FLEET in currentPermissions
    val canManagePermissions = Permission.MANAGE_PERMISSIONS in currentPermissions
    val canPost = Permission.POST_ENTRY in currentPermissions

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
                Routes.FLEET -> "Fleet connection"
                Routes.PERMISSIONS -> "Permissions"
                Routes.PDF_LIST -> "PDF list"
                else -> "Engine Log"
            },
            canEditForm = canEdit,
            canManageCrew = canManageCrew,
            canManageFleet = canManageFleet,
            canManagePermissions = canManagePermissions,
            onManageGroups = { navController.navigate(Routes.MANAGE_GROUPS) },
            onManageCrew = { navController.navigate(Routes.MANAGE_CREW) },
            onJournal = { navController.navigate(Routes.journalWith(currentCrewId, currentRank)) },
            onPdfList = { navController.navigate(Routes.PDF_LIST) },
            onEntry = { navController.popBackStack(Routes.HOME, inclusive = false) },
            onFleet = { navController.navigate(Routes.FLEET) },
            onPermissions = { navController.navigate(Routes.PERMISSIONS) },
            onSignOut = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) { paddingModifier ->
            AppNavGraph(
                navController = navController,
                modifier = paddingModifier,
                currentCrewId = currentCrewId,
                startDestination = startDestination!!,
                canPost = canPost,
                canEditForm = canEdit,
                onLoginResolved = { crewId, rank ->
                    currentCrewId = crewId
                    currentRank = rank
                }
            )
        }

    } else {
        AppNavGraph(
            navController = navController,
            modifier = modifier,
            currentCrewId = currentCrewId,
            startDestination = startDestination!!,
            canPost = canPost,
            canEditForm = canEdit,
            onLoginResolved = { crewId, rank ->
                currentCrewId = crewId
                currentRank = rank
            }
        )
    }
}

@Composable
private fun AppNavGraph(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
    currentCrewId: Long,
    startDestination: String,
    canPost: Boolean,
    canEditForm: Boolean,
    onLoginResolved: (Long, String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(Routes.MANAGE_CREW) {
            ManageCrewScreen(
                activeCrewId = currentCrewId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { crewId, rank  ->
                    onLoginResolved(crewId, rank )
                    navController.navigate(Routes.homeWith(crewId, rank )) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FLEET) {
            FleetScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.MANAGE_GROUPS) {
            ManageGroupsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.JOURNAL,
            arguments = listOf(
                navArgument("crewId") { type = NavType.LongType },
                navArgument("rank") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            JournalScreen(
                crewId = backStackEntry.arguments?.getLong("crewId") ?: 0L,
                canPost = canPost
            )
        }

        composable(Routes.PDF_LIST) {
            PdfListScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PERMISSIONS) {
            PermissionMatrixScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.HOME,
            arguments = listOf(
                navArgument("crewId") { type = NavType.LongType },
                navArgument("rank") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val crewId = backStackEntry.arguments?.getLong("crewId") ?: 0L
            val rank = backStackEntry.arguments?.getString("rank") ?: "OILER"
            LogEntryScreen(
                crewId = crewId,
                canEditForm = canEditForm,
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