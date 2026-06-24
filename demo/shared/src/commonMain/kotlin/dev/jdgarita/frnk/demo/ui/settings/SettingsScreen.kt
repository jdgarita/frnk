package dev.jdgarita.frnk.demo.ui.settings

import androidx.compose.runtime.Composable
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
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
fun SettingsScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateAway: () -> Unit
) {
    val controller = LocalAppearanceController.current

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

                CommonUiEffect.DidPressBack() -> onNavigateAway()
                else -> Unit
            }
        }
    ) { state ->
        FrnkSettingsScreen(
            state = state,
            onIntent = viewModel::send
        )
    }
}