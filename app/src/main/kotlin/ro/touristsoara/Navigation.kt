package ro.touristsoara

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ro.touristsoara.data.PoiDataProvider
import ro.touristsoara.model.Language
import ro.touristsoara.ui.input.InputScreen
import ro.touristsoara.ui.map.MapScreen
import ro.touristsoara.ui.map.RouteMapScreen
import ro.touristsoara.ui.poi.PoiDetailSheet
import ro.touristsoara.ui.poimanager.PoiListScreen
import ro.touristsoara.ui.summary.SummaryScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(ScreenInputKey)
    var currentLanguage by remember { mutableStateOf(Language.RO) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Map> {
                    MapScreen()
                }
                entry<Main> {
                    InputScreen(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { currentLanguage = it },
                        onPlanRoute = { start, destination, budget, mode, interests ->
                            val route = PoiDataProvider.calculateGreenRoute(
                                startName = start,
                                destinationName = destination,
                                budgetMinutes = budget,
                                travelMode = mode,
                                interests = interests,
                                language = currentLanguage
                            )
                            backStack.add(ScreenRouteMapKey(route))
                        },
                        onOpenPoiManager = { backStack.add(ScreenPoiManagerKey) }
                    )
                }
                entry<ScreenInputKey> {
                    InputScreen(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { currentLanguage = it },
                        onPlanRoute = { start, destination, budget, mode, interests ->
                            val route = PoiDataProvider.calculateGreenRoute(
                                startName = start,
                                destinationName = destination,
                                budgetMinutes = budget,
                                travelMode = mode,
                                interests = interests,
                                language = currentLanguage
                            )
                            backStack.add(ScreenRouteMapKey(route))
                        },
                        onOpenPoiManager = { backStack.add(ScreenPoiManagerKey) }
                    )
                }
                entry<ScreenRouteMapKey> { key ->
                    RouteMapScreen(
                        route = key.route,
                        currentLanguage = currentLanguage,
                        onBackToInput = { backStack.removeLastOrNull() },
                        onOpenPoiDetail = { poi -> backStack.add(ScreenPoiDetailKey(poi)) },
                        onFinishTour = { backStack.add(ScreenSummaryKey(key.route)) }
                    )
                }
                entry<ScreenPoiDetailKey> { key ->
                    PoiDetailSheet(
                        poi = key.poi,
                        currentLanguage = currentLanguage,
                        onClose = { backStack.removeLastOrNull() },
                        onContinueRoute = { backStack.removeLastOrNull() }
                    )
                }
                entry<ScreenPoiManagerKey> {
                    PoiListScreen(
                        currentLanguage = currentLanguage,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<ScreenSummaryKey> { key ->
                    SummaryScreen(
                        route = key.route,
                        currentLanguage = currentLanguage,
                        onPlanNewRoute = {
                            while (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        }
                    )
                }
            },
    )
}
