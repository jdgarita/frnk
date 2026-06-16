package dev.jdgarita.frnk.ui.scaffolds.settings

import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.mergedWith
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.withTheme
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.withToggle

/**
 * Thin UI-state machine for [SettingsScreen]. Owns nothing but the rendered row state: theme
 * selection and toggle values are reduced into the state *and* surfaced as effects, so the host
 * decides what they mean (apply the appearance, persist the toggle, navigate, restore purchases, …)
 * by collecting [SettingsEffect].
 */
class SettingsViewModel(
    initial: SettingsScreenState,
) : MviViewModel<SettingsScreenState, SettingsIntent, SettingsEffect>(initial) {
    override suspend fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeSelected -> {
                setState { withTheme(intent.appearance) }
                emit(SettingsEffect.AppearanceChanged(intent.appearance))
            }

            is SettingsIntent.ToggleChanged -> {
                setState { withToggle(intent.id, intent.checked) }
                emit(SettingsEffect.ToggleChanged(intent.id, intent.checked))
            }

            is SettingsIntent.RowClicked -> emit(SettingsEffect.ActionInvoked(intent.action))
            is SettingsIntent.ConfigChanged -> setState { mergedWith(intent.newState) }
            SettingsIntent.VersionTapped ->
                setState {
                    if (developerRevealed) {
                        this
                    } else {
                        val taps = versionTapCount + 1
                        copy(
                            versionTapCount = taps,
                            developerRevealed = taps >= SettingsScreenState.DEVELOPER_REVEAL_TAPS,
                        )
                    }
                }
        }
    }
}
