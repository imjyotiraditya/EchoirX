package app.echoirx.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import app.echoirx.domain.model.SearchResult
import app.echoirx.presentation.navigation.Route
import app.echoirx.presentation.navigation.components.EchoirBottomNav
import app.echoirx.presentation.navigation.components.EchoirTopBar
import app.echoirx.presentation.screens.details.DetailsScreen
import app.echoirx.presentation.screens.home.HomeScreen
import app.echoirx.presentation.screens.search.SearchScreen
import app.echoirx.presentation.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.let { Route.fromPath(it) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EchoirTopBar(
                currentRoute = currentRoute,
                onNavigateBack = { navController.popBackStack() },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            EchoirBottomNav(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        modifier = Modifier,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.path,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Home.path) {
                HomeScreen(snackbarHostState = snackbarHostState)
            }
            composable(Route.Search.Main.path) {
                SearchScreen(navController, snackbarHostState = snackbarHostState)
            }
            composable(
                route = Route.Search.Details().createRoute(),
                arguments = listOf(
                    navArgument(Route.Search.Details.TYPE_ARG) { type = NavType.StringType },
                    navArgument(Route.Search.Details.ID_ARG) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString(Route.Search.Details.TYPE_ARG)
                val id = backStackEntry.arguments?.getLong(Route.Search.Details.ID_ARG)
                val result = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<SearchResult>("result")

                if (type != null && id != null && result != null) {
                    DetailsScreen(result, snackbarHostState = snackbarHostState)
                }
            }
            composable(Route.Settings.path) {
                SettingsScreen()
            }
        }
    }
}