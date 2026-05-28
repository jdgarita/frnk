package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.ui.theme.Appearance

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
 * [options] are the values emitted via [SettingsIntent.ThemeSelected]; [optionLabels] is the
 * parallel list of display strings shown in the control (resolve from `FrnkStrings` at build time).
 */
@Immutable
data class SettingsThemeRowState(
    override val id: String = "theme",
    val title: String,
    val icon: FrnkIconState? = null,
    val selected: Appearance,
    val options: List<Appearance> = listOf(Appearance.System, Appearance.Light, Appearance.Dark),
    val optionLabels: List<String>,
) : SettingsRowState

/** Row style 2 — icon + title + optional subtitle + chevron. Fires [action] on click. */
@Immutable
data class SettingsClickableRowState(
    override val id: String,
    val icon: FrnkIconState,
    val title: String,
    val subtitle: String? = null,
    val action: SettingsAction,
) : SettingsRowState

/** Row style 3 — icon + title + optional subtitle + switch. */
@Immutable
data class SettingsToggleRowState(
    override val id: String,
    val icon: FrnkIconState,
    val title: String,
    val subtitle: String? = null,
    val checked: Boolean,
) : SettingsRowState

/** A card grouping related rows, with an optional header [title] and trailing [footnote] help text. */
@Immutable
data class SettingsSectionState(
    val title: String? = null,
    val rows: List<SettingsRowState>,
    val footnote: String? = null,
)

/**
 * Footer copy. [text] defaults (via the catalog) to the `stringSettingsFooter` token —
 * "Made in 🇨🇷 by JD with" — followed by a coffee icon when [showCoffeeIcon] is true. [version] is
 * the *host app's* version string (the host passes its own `BuildConfig.versionName`).
 */
@Immutable
data class SettingsFooterState(
    val text: String,
    val showCoffeeIcon: Boolean = true,
    val version: String,
)

/**
 * Configuration + runtime state for [SettingsScreen]. The screen is a list of [sections] (each a
 * card of [SettingsRowState] rows) plus an optional [footer]. State mutations flow through the
 * ViewModel reducer; hosts build the initial state by hand or via
 * [rememberDefaultSettingsState][dev.jdgarita.frnk.ui.scaffolds.rememberDefaultSettingsState].
 */
@Immutable
data class SettingsScreenState(
    val title: String = "Settings",
    val sections: List<SettingsSectionState>,
    val footer: SettingsFooterState? = null,
) : UiState

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
