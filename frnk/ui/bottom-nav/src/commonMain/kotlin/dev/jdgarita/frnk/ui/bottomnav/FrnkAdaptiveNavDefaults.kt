package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringNavHome
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.strings

/**
 * Builds the toolkit's fixed three-tab [FrnkBottomNavState] for [FrnkTabbedNavScaffold]: a fixed
 * **Home** tab, the host's configurable center **[feature]** tab, then a fixed **Settings** tab — the
 * `Home · feature · Settings` shape the adaptive bar always renders. Home and Settings carry the
 * toolkit's theme icons ([iconNavHome]/[iconSettings] `ImageVector`s for Android) + SF-Symbol names
 * (`"house"`/`"gearshape"` for iOS), with labels from `stringNavHome`/`stringSettings`. Hosts re-skin
 * the bookend icons/labels through `FrnkThemeConfig`; the center tab is shaped by [feature].
 *
 * The host supplies each bookend's back-stack [homeRoot]/[settingsRoot] (the `NavKey` its tab starts
 * from) since routes are host-defined; the [feature] tab carries its own route + icons.
 *
 * **Pass a stable [feature]** — `remember` the [FrnkFeatureItem] rather than constructing it inline
 * every recomposition. This builder keys its `remember` on [feature], so a freshly-built item each
 * frame busts this cache → a new state every frame → `FrnkTabbedNavScaffold` (and the derived back
 * stacks) become non-skippable.
 *
 * @param homeRoot the Home tab's back-stack root destination.
 * @param settingsRoot the Settings tab's back-stack root destination.
 * @param feature the host's configurable center tab (route + icons + label).
 * @param homeKey stable key for the Home tab.
 * @param settingsKey stable key for the Settings tab.
 */
@Composable
fun rememberFrnkBottomNavState(
    homeRoot: NavKey,
    settingsRoot: NavKey,
    feature: FrnkFeatureItem,
    homeKey: String = "home",
    settingsKey: String = "settings"
): FrnkBottomNavState {
    // The three tab keys back per-tab back stacks and drive selection (indexOfFirst by key) — they must
    // be distinct, or switching/selecting the feature tab would silently target the Home/Settings stack.
    require(feature.key != homeKey && feature.key != settingsKey) {
        "feature.key ('${feature.key}') must differ from homeKey ('$homeKey') and settingsKey ('$settingsKey')"
    }

    val homeLabel = Theme[strings][stringNavHome]
    val settingsLabel = Theme[strings][stringSettings]
    val homeIcon = Theme[icons][iconNavHome]
    val settingsIcon = Theme[icons][iconSettings]

    return remember(
        homeRoot,
        settingsRoot,
        feature,
        homeKey,
        settingsKey,
        homeLabel,
        settingsLabel,
        homeIcon,
        settingsIcon
    ) {
        FrnkBottomNavState(
            home =
                FrnkBottomNavTab.Home(
                    key = homeKey,
                    root = homeRoot,
                    label = homeLabel,
                    icon = homeIcon,
                    iosSystemIcon = "house"
                ),
            feature =
                FrnkBottomNavTab.Feature(
                    key = feature.key,
                    root = feature.route,
                    label = feature.label,
                    icon = feature.icon,
                    iosSystemIcon = feature.iosSystemIcon
                ),
            settings =
                FrnkBottomNavTab.Settings(
                    key = settingsKey,
                    root = settingsRoot,
                    label = settingsLabel,
                    icon = settingsIcon,
                    iosSystemIcon = "gearshape"
                )
        )
    }
}