package dev.jdgarita.frnk.demo.ui.settings

import androidx.compose.runtime.Composable
import dev.jdgarita.frnk.ui.haptics.HAPTICS_TOGGLE_ID
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.scaffolds.settings.FrnkSettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsArguments
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsViewModel
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(onNavigateToOnboarding: () -> Unit) {
    val controller = LocalAppearanceController.current
    // FrnkApp builds this over the demo's HapticsPreference, so setEnabled persists the switch.
    val haptics = LocalFrnkHaptics.current

    val viewModel: SettingsViewModel = koinViewModel()
    val sendFeedback = rememberFeedbackEmailLauncher(appName = "Frnk", appVersion = "0.0.0.312")

    FrnkScreen(
        arguments = SettingsArguments,
        viewModel = viewModel,
        onEffect = { uiEffect ->
            when (uiEffect) {
                is SettingsEffect.AppearanceChanged -> controller.appearance = uiEffect.appearance
                is SettingsEffect.ActionInvoked ->
                    when (uiEffect.action) {
                        SettingsAction.ShowOnboarding -> onNavigateToOnboarding()
                        SettingsAction.SendFeedback -> sendFeedback()
                        else -> Unit
                    }

                is SettingsEffect.ToggleChanged ->
                    if (uiEffect.id == HAPTICS_TOGGLE_ID) haptics.setEnabled(uiEffect.checked)
            }
        }
    ) { state ->
        FrnkSettingsScreen(
            state = state,
            onIntent = viewModel::send
        )
    }
}