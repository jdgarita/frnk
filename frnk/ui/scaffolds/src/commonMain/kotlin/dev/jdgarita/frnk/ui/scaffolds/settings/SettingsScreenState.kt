package dev.jdgarita.frnk.ui.scaffolds.settings

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.stringAppearance
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.stringThemeDark
import dev.jdgarita.frnk.ui.theme.stringThemeLight
import dev.jdgarita.frnk.ui.theme.stringThemeSystem

/**
 * Typed catalogue of the actions a clickable settings row can fire. The toolkit ships the common
 * ones; [Custom] lets a host add app-specific rows without forking the scaffold. The host decides
 * what each action *does* by collecting [SettingsEffect.ActionInvoked] — the scaffold never opens a
 * URL, shows a paywall, or restores purchases itself.
 */
sealed interface SettingsAction {
    data object UpgradeToPro : SettingsAction

    data object RestorePurchases : SettingsAction

    data object ManageSubscription : SettingsAction

    data object ShowOnboarding : SettingsAction

    data object SendFeedback : SettingsAction

    data object RateApp : SettingsAction

    data object PrivacyPolicy : SettingsAction

    data object TermsOfService : SettingsAction

    data class Custom(
        val id: String,
    ) : SettingsAction
}

/** One row in a settings section. [id] is the stable key used for list diffing and toggle updates. */
@Immutable
sealed interface SettingsRowState {
    val id: String
}

/**
 * Row style 1 — a three-way (or n-way) segmented toggle. Used for theme selection but generic.
 * [options] are the values emitted via [SettingsIntent.ThemeSelected]; [optionLabels] is the parallel
 * list of display labels (theme-token [FrnkStringSource]s, resolved by the renderer — not baked here).
 */
@Immutable
data class SettingsThemeRowState(
    override val id: String = "theme",
    val title: FrnkStringSource,
    val icon: FrnkIconSource? = null,
    val selected: Appearance,
    val options: List<Appearance> = listOf(Appearance.System, Appearance.Light, Appearance.Dark),
    val optionLabels: List<FrnkStringSource>,
) : SettingsRowState

/** Row style 2 — icon + title + optional subtitle + chevron. Fires [action] on click. */
@Immutable
data class SettingsClickableRowState(
    override val id: String,
    val icon: FrnkIconSource,
    val title: FrnkStringSource,
    val subtitle: FrnkStringSource? = null,
    val action: SettingsAction,
) : SettingsRowState

/** Row style 3 — icon + title + optional subtitle + switch. */
@Immutable
data class SettingsToggleRowState(
    override val id: String,
    val icon: FrnkIconSource,
    val title: FrnkStringSource,
    val subtitle: FrnkStringSource? = null,
    val checked: Boolean,
) : SettingsRowState

/**
 * Row style 4 — a **non-interactive** status row: icon + title + optional subtitle + a trailing
 * [badge] pill. Used to surface state that needs no action, e.g. a "Pro Member" / `PRO` badge once the
 * user is entitled. Has no [SettingsAction]; tapping it does nothing.
 */
@Immutable
data class SettingsStatusRowState(
    override val id: String,
    val icon: FrnkIconSource,
    val title: FrnkStringSource,
    val subtitle: FrnkStringSource? = null,
    val badge: FrnkStringSource? = null,
) : SettingsRowState

/** A card grouping related rows, with an optional header [title] and trailing [footnote] help text. */
@Immutable
data class SettingsSectionState(
    val title: FrnkStringSource? = null,
    val rows: List<SettingsRowState>,
    val footnote: FrnkStringSource? = null,
)

/**
 * Footer copy. [text] defaults (via the catalog) to the `stringSettingsFooter` token —
 * "Built by JD in 🇨🇷". [version] is the *host app's* version string (the host passes its own
 * `BuildConfig.versionName`).
 */
@Immutable
data class SettingsFooterState(
    val text: FrnkStringSource,
    val version: FrnkStringSource.Raw,
)

/**
 * Configuration + runtime state for [SettingsScreen]. The screen is a list of [sections] (each a
 * card of [SettingsRowState] rows) plus an optional [footer]. State mutations flow through the
 * ViewModel reducer; hosts build the initial state by hand or via
 * [defaultSettingsState][defaultSettingsState].
 */
@Immutable
data class SettingsScreenState(
    val title: FrnkStringSource = FrnkStringSource.Token(stringSettings),
    // Minimal token-only default so the state is constructible (and the VM seedable) without
    // composition; the full catalogue is built by [defaultSettingsState].
    val sections: List<SettingsSectionState> =
        listOf(
            SettingsSectionState(
                rows =
                    listOf(
                        SettingsThemeRowState(
                            title = FrnkStringSource.Token(stringAppearance),
                            selected = Appearance.System,
                            optionLabels =
                                listOf(
                                    FrnkStringSource.Token(stringThemeSystem),
                                    FrnkStringSource.Token(stringThemeLight),
                                    FrnkStringSource.Token(stringThemeDark),
                                ),
                        ),
                    ),
            ),
        ),
    val footer: SettingsFooterState? = null,
    /**
     * Optional hidden section (e.g. a god-mode toggle) shown only once revealed. Revealed by tapping
     * the version footer [DEVELOPER_REVEAL_TAPS] times, or immediately when [showDeveloperSection] is
     * true (a host opt-in for internal builds). Lets a developer flip god mode even in a release build.
     */
    val developerSection: SettingsSectionState? = null,
    val showDeveloperSection: Boolean = false,
    val developerRevealed: Boolean = false,
    val versionTapCount: Int = 0,
) : UiState {
    val developerVisible: Boolean get() = developerSection != null && (showDeveloperSection || developerRevealed)

    companion object {
        const val DEVELOPER_REVEAL_TAPS = 7
    }
}

sealed interface SettingsIntent : UiIntent {
    data class ThemeSelected(
        val appearance: Appearance,
    ) : SettingsIntent

    data class ToggleChanged(
        val id: String,
        val checked: Boolean,
    ) : SettingsIntent

    data class RowClicked(
        val action: SettingsAction,
    ) : SettingsIntent

    /** The version footer was tapped; enough taps reveal the hidden developer section. */
    data object VersionTapped : SettingsIntent

    /**
     * The host recomputed the catalogue (e.g. `isPro` flipped, or a string/icon/`extraSections`
     * override changed) and handed down a fresh [newState]. The reducer adopts [newState]'s
     * structure (subscription rows, titles, icons, footer, section list) while preserving the
     * VM-owned interaction state — toggle `checked` values, the selected theme, and the version-tap
     * dev-reveal progress — so reacting to entitlement changes never resets the user's in-session
     * choices. This replaces the old "re-key the ViewModel to re-seed it" approach.
     */
    data class ConfigChanged(
        val newState: SettingsScreenState,
    ) : SettingsIntent
}

sealed interface SettingsEffect : UiEffect {
    /** The user picked a theme. The host applies it to its [Appearance] controller (and persists). */
    data class AppearanceChanged(
        val appearance: Appearance,
    ) : SettingsEffect

    /** A toggle row flipped. The host persists the new value for [id]. */
    data class ToggleChanged(
        val id: String,
        val checked: Boolean,
    ) : SettingsEffect

    /** A clickable row was tapped. The host performs the [action] (open URL, paywall, restore, …). */
    data class ActionInvoked(
        val action: SettingsAction,
    ) : SettingsEffect
}
