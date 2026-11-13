package ro.touristsoara

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ro.touristsoara.ui.map.MapScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Map)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Map> {
          MapScreen()
        }
      },
  )
}
