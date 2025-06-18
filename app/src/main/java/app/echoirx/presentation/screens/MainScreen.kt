package app.echoirx.presentation.screens

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.echoirx.domain.model.SearchResult
import app.echoirx.presentation.navigation.Route
import app.echoirx.presentation.navigation.components.EchoirBottomNav
import app.echoirx.presentation.navigation.components.EchoirTopBar
import app.echoirx.presentation.screens.details.DetailsScreen
import app.echoirx.presentation.screens.home.HomeScreen
import app.echoirx.presentation.screens.search.SearchScreen
import app.echoirx.presentation.screens.search.SearchType
import app.echoirx.presentation.screens.settings.SettingsScreen
import com.bobbyesp.foundation.ui.motion.animatedComposable
import com.bobbyesp.foundation.ui.motion.slideInVerticallyComposable
import com.bobbyesp.foundation.util.navigation.serializableType
import kotlin.reflect.typeOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    //Returns the qualified name of the current destination
    val currentDestination = navBackStackEntry?.destination

    val currentTopLevelRoute = remember(navBackStackEntry) {
        currentDestination
            ?.hierarchy
            ?.mapNotNull { dest ->
                Route.topLevelRoutes.find { it.qualifiedName == dest.route }
            }
            ?.firstOrNull()
    }

    val isSearchDetails = remember(navBackStackEntry) {
        currentDestination?.hasRoute(Route.SearchNavigator.Details::class)
            ?: false
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EchoirTopBar(
                currentTopLevelRoute = currentTopLevelRoute ?: Route.HomeNavigator,
                isSearchDetails = isSearchDetails,
                onNavigateBack = { navController.popBackStack() },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            EchoirBottomNav(
                navController = navController, currentRoute = currentTopLevelRoute
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState, modifier = Modifier, snackbar = {
                    Snackbar(
                        snackbarData = it,
                        modifier = Modifier,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                })
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        NavHost(
            navController = navController,
            route = Route.MainHost::class,
            startDestination = Route.HomeNavigator::class,
            modifier = Modifier.padding(innerPadding)
        ) {
            navigation<Route.HomeNavigator>(
                startDestination = Route.HomeNavigator.Home
            ) {
                animatedComposable<Route.HomeNavigator.Home> {
                    HomeScreen(snackbarHostState = snackbarHostState)
                }
            }
            navigation<Route.SearchNavigator>(
                startDestination = Route.SearchNavigator.Search
            ) {
                animatedComposable<Route.SearchNavigator.Search> {
                    SearchScreen(navController, snackbarHostState = snackbarHostState)
                }

                slideInVerticallyComposable<Route.SearchNavigator.Details>(
                    typeMap = mapOf(
                        typeOf<SearchType>() to serializableType<SearchType>(),
                        typeOf<Long>() to NavType.LongType
                    )
                ) { backStackEntry ->
                    val data = backStackEntry.toRoute<Route.SearchNavigator.Details>() //How to get the data
                    val result =
                        navController.previousBackStackEntry?.savedStateHandle?.get<SearchResult>("result")

                    result?.let {
                        DetailsScreen(
                            result = result, snackbarHostState = snackbarHostState
                        )
                    }
                }
            }

            navigation<Route.SettingsNavigator>(
                startDestination = Route.SettingsNavigator.Settings
            ) {
                animatedComposable<Route.SettingsNavigator.Settings> {
                    SettingsScreen()
                }
            }
        }
    }
}