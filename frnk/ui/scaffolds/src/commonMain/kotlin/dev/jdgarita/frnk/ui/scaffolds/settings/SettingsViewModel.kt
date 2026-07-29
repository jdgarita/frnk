package dev.jdgarita.frnk.ui.scaffolds.settings

import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.ui.mvi.ModelStateFactory
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.mergedWith
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.toUiState
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.withTheme
import dev.jdgarita.frnk.ui.scaffolds.settings.ext.withToggle
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin UI-state machine for [FrnkSettingsScreen]. Owns nothing but the rendered row state: theme
 * selection and toggle values are reduced into the state *and* surfaced as effects, so the host
 * decides what they mean (apply the appearance, persist the toggle, navigate, restore purchases, …)
 * by collecting [SettingsEffect].
 *
 * The initial model is the factory's [SettingsModelState.DEFAULT]; the host's recomputed catalogue is
 * folded in later via [SettingsIntent.ConfigChanged] (`mergedWith`), preserving in-session interaction
 * state (toggle checks, version-tap dev-reveal).
 *
 * @param observeProStatus the Free/Pro use case, resolved via Koin. Exposed as [isPro] for internal
 *   use — groundwork for the Settings VM to own the subscription catalogue (today the Free/Pro rows
 *   are still built upstream).
 */
class SettingsViewModel(
    observeProStatus: ObserveProStatusUseCase
) : MviViewModel<SettingsArguments, SettingsModelState, SettingsScreenState, SettingsIntent, SettingsEffect>(
        factory =
            object : ModelStateFactory<SettingsModelState> {
                override fun initialModelState() = SettingsModelState.DEFAULT
            },
        mapper = SettingsModelState::toUiState
    ) {
    /** Reactive Free/Pro status. */
    val isPro: StateFlow<Boolean> = observeProStatus.invoke()

    override suspend fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeSelected -> {
                updateModel {
                    withTheme(intent.appearance)
                }
                emit(SettingsEffect.AppearanceChanged(intent.appearance))
            }

            is SettingsIntent.ToggleChanged -> {
                updateModel { withToggle(intent.id, intent.checked) }
                emit(SettingsEffect.ToggleChanged(intent.id, intent.checked))
            }

            is SettingsIntent.RowClicked -> emit(SettingsEffect.ActionInvoked(intent.action))
            is SettingsIntent.ConfigChanged -> updateModel { mergedWith(intent.newState) }
            SettingsIntent.VersionTapped ->
                updateModel {
                    if (developerRevealed) {
                        this
                    } else {
                        val taps = versionTapCount + 1
                        copy(
                            versionTapCount = taps,
                            developerRevealed = taps >= SettingsScreenState.DEVELOPER_REVEAL_TAPS
                        )
                    }
                }
        }
    }
}