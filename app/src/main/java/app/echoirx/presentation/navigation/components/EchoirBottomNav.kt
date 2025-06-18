package app.echoirx.presentation.navigation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import app.echoirx.presentation.navigation.NavConstants
import app.echoirx.presentation.navigation.Route
import app.echoirx.presentation.navigation.Route.Companion.isOfType
import app.echoirx.presentation.navigation.navigationItems

@Composable
fun EchoirBottomNav(
    navController: NavHostController,
    currentRoute: Route?
) {
    NavigationBar {
        navigationItems.forEach { item ->
            val isSelected = remember(currentRoute, item.route) {
                currentRoute?.isOfType(item.route::class) ?: false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set(NavConstants.KEY_FOCUS_SEARCH_BAR, true)
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.label),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}