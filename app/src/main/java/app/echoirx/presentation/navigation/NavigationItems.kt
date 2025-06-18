package app.echoirx.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.echoirx.R

data class NavigationItem(
    val route: Route,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int
)

val navigationItems = listOf(
    NavigationItem(
        route = Route.HomeNavigator,
        icon = R.drawable.ic_home,
        label = R.string.nav_home
    ),
    NavigationItem(
        route = Route.SearchNavigator,
        icon = R.drawable.ic_search,
        label = R.string.nav_search
    ),
    NavigationItem(
        route = Route.SettingsNavigator,
        icon = R.drawable.ic_settings,
        label = R.string.nav_settings
    )
)