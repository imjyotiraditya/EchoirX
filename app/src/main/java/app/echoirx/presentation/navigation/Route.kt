package app.echoirx.presentation.navigation

import androidx.annotation.StringRes
import app.echoirx.R
import app.echoirx.presentation.screens.search.SearchType
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
sealed interface Route {

    @Serializable
    sealed interface TopLevelRoute : Route {
        val qualifiedName: String
            get() = this::class.qualifiedName ?: "Unknown"
    }

    @Serializable
    data object MainHost : Route

    @Serializable
    data object HomeNavigator : TopLevelRoute, Route {

        @Serializable
        data object Home : Route
    }

    @Serializable
    data object SettingsNavigator : TopLevelRoute, Route {

        @Serializable
        data object Settings : Route
    }

    @Serializable
    data object SearchNavigator : TopLevelRoute, Route {
        @Serializable
        data object Search : Route

        @Serializable
        data class Details(val type: SearchType, val id: Long = -1) : Route
    }

    companion object {
        val topLevelRoutes: List<TopLevelRoute> = listOf(
            HomeNavigator,
            SearchNavigator,
            SettingsNavigator
        )

        val TopLevelRoute.navigationLabel: Int
            @StringRes get() = when (this) {
                HomeNavigator -> R.string.nav_home
                SearchNavigator -> R.string.nav_search
                SettingsNavigator -> R.string.nav_settings
            }

        fun Route.isTopLevel(): Boolean = this in topLevelRoutes

        fun <T : Route> Route.isOfType(type: KClass<out T>): Boolean {
            return this::class == type
        }
    }
}