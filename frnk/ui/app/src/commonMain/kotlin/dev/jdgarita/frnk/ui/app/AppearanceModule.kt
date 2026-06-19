package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.ui.theme.AppearanceController
import org.koin.dsl.module

/**
 * Registers the app-wide [AppearanceController] so [FrnkApp] (and any other DI consumer) can resolve
 * it via Koin. It's a [single] because the light/dark/system selection is one shared, app-lifetime
 * piece of UI state — re-creating it per resolution would lose the user's in-session toggle.
 *
 * Bundled into [frnkUiModules] so every host gets it without extra wiring.
 */
val appearanceModule =
    module {
        single { AppearanceController() }
    }