package ro.touristsoara

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ro.touristsoara.model.Poi
import ro.touristsoara.model.RouteResult

@Serializable
data object Main : NavKey

@Serializable
data object ScreenInputKey : NavKey

@Serializable
data class ScreenRouteMapKey(val route: RouteResult) : NavKey

@Serializable
data class ScreenPoiDetailKey(val poi: Poi) : NavKey

@Serializable
data object ScreenPoiManagerKey : NavKey

@Serializable
data class ScreenSummaryKey(val route: RouteResult) : NavKey
