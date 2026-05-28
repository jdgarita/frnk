package dev.jdgarita.frnk.ui.scaffolds

import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.theme.Appearance

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
        }
    }
}

/** Reduce the selected theme on every [SettingsThemeRowState] in the state. */
private fun SettingsScreenState.withTheme(appearance: Appearance): SettingsScreenState =
    mapRows { row ->
        if (row is SettingsThemeRowState) row.copy(selected = appearance) else row
    }

/** Reduce the checked value of the [SettingsToggleRowState] whose id matches [id]. */
private fun SettingsScreenState.withToggle(
    id: String,
    checked: Boolean,
): SettingsScreenState =
    mapRows { row ->
        if (row is SettingsToggleRowState && row.id == id) row.copy(checked = checked) else row
    }

private fun SettingsScreenState.mapRows(transform: (SettingsRowState) -> SettingsRowState): SettingsScreenState =
    copy(sections = sections.map { section -> section.copy(rows = section.rows.map(transform)) })
