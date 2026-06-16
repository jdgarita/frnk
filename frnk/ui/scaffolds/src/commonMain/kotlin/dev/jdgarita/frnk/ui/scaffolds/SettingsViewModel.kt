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
    copy(
        sections = sections.map { section -> section.copy(rows = section.rows.map(transform)) },
        // The hidden developer section carries real interactive rows too (e.g. a god-mode toggle), so
        // its rows must reduce alongside the visible ones — otherwise ToggleChanged never updates them.
        developerSection = developerSection?.let { section -> section.copy(rows = section.rows.map(transform)) },
    )

/**
 * Adopt [incoming]'s structure — the catalogue the host just recomputed (e.g. the Free/Pro
 * subscription rows, titles, icons, footer, section order) — while preserving this VM's interaction
 * state: every toggle's `checked` value (matched by row [id][SettingsRowState.id]) and the version-tap
 * dev-reveal progress. Both the visible [sections] and the hidden
 * [developerSection][SettingsScreenState.developerSection] are reconciled (the latter carries a
 * host's god-mode toggle).
 *
 * The selected **theme is taken from [incoming]**, not preserved. Appearance has a single source of
 * truth — the host's appearance controller, which [incoming] is built from — so a change from *any*
 * path (the Settings toggle or elsewhere) is reflected. The toggle's own optimistic feedback comes
 * from [withTheme] on `ThemeSelected`, not from this merge.
 *
 * Edge: the merge lets the VM's value win over an externally-driven *toggle* default in [incoming].
 * No toolkit path relies on the incoming default winning — interactive toggles round-trip through the
 * host (via [SettingsEffect.ToggleChanged]) and flow back as the next [incoming], so the two agree in
 * steady state.
 */
private fun SettingsScreenState.mergedWith(incoming: SettingsScreenState): SettingsScreenState {
    val checkedById =
        (sections + listOfNotNull(developerSection))
            .flatMap { it.rows }
            .filterIsInstance<SettingsToggleRowState>()
            .associate { it.id to it.checked }

    fun reconcile(section: SettingsSectionState): SettingsSectionState =
        section.copy(
            rows =
                section.rows.map { row ->
                    if (row is SettingsToggleRowState) checkedById[row.id]?.let { row.copy(checked = it) } ?: row else row
                },
        )

    // VM-owned interaction state carried across a config swap: toggle `checked` (reconciled above) and
    // the dev-reveal counters below. Everything else — title, footer, sections, the selected theme — is
    // adopted from [incoming]. When adding a new *user-mutated* field to SettingsScreenState, preserve
    // it here too, or it will silently reset to the incoming seed on every ConfigChanged.
    return incoming.copy(
        sections = incoming.sections.map(::reconcile),
        developerSection = incoming.developerSection?.let(::reconcile),
        versionTapCount = versionTapCount,
        developerRevealed = developerRevealed,
    )
}
