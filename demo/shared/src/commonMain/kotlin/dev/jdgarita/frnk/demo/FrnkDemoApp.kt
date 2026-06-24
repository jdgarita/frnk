package dev.jdgarita.frnk.demo

import androidx.compose.runtime.Composable
import dev.jdgarita.frnk.demo.navigation.modules.rootNavigationModule
import dev.jdgarita.frnk.ui.app.FrnkApp
import dev.jdgarita.frnk.ui.nav.frnkRootNavConfig

@Composable
fun FrnkDemoApp() {
    FrnkApp(
        onSavedStateConfiguration = { frnkRootNavConfig },
        onNavigationModule = { backStack ->
            rootNavigationModule(backStack = backStack)
        }
    )
}