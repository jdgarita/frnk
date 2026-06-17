package dev.jdgarita.frnk.ui.scaffolds.settings

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for the settings scaffold. Hosts that use the VM-backed [SettingsScreen] convenience
 * composable must install this module (or `includes(settingsScaffoldModule)` it from a parent module).
 *
 * The ViewModel takes the initial [SettingsScreenState] as a runtime parameter, so the call site
 * passes the configured sections via `parametersOf(initialState)` — the catalog stays at the
 * composable boundary rather than in the DI graph.
 *
 * The `ObserveProStatusUseCase` is resolved via Koin (`get()`) — its binding ships in
 * `monetizationModule`, so hosts that use the VM-backed Settings scaffold must install monetization.
 */
val settingsScaffoldModule =
    module {
        viewModel { params -> SettingsViewModel(initial = params.get(), observeProStatus = get()) }
    }
