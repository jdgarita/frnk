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
 * Builds the default [FrnkAdaptiveNavTab] list for [FrnkTabbedNavScaffold], enforcing the toolkit's
 * product contract — **every app has at least Home and Settings** — for the nav3 multiple-back-stack
 * path: a fixed **Home** tab, the host's optional [middleTabs], then a fixed
 * **Settings** tab. Home and Settings carry the toolkit's theme icons ([iconNavHome]/[iconSettings]
 * `ImageVector`s for Android) + SF-Symbol names (`"house"`/`"gearshape"` for iOS), with labels from
 * `stringNavHome`/`stringSettings`. Hosts re-skin the icons/labels through `FrnkThemeConfig`.
 *
 * The host supplies each bookend's back-stack [homeRoot]/[settingsRoot] (the `NavKey` its tab starts
 * from) since routes are host-defined; middle tabs (with their own icons/roots) slot between them.
 *
 * **Pass a stable [middleTabs] list** — `remember` each `FrnkAdaptiveNavTab` (or the whole list) rather
 * than constructing them inline every recomposition. This builder keys its `remember` on [middleTabs], so
 * a freshly-built tab each frame busts this cache → a new tab list every frame → `FrnkTabbedNavScaffold`
 * (and the derived back stacks) become non-skippable.
 *
 * @param homeRoot the Home tab's back-stack root destination.
 * @param settingsRoot the Settings tab's back-stack root destination.
 * @param middleTabs the host's configurable destinations between Home and Settings.
 * @param homeKey stable key for the Home tab.
 * @param settingsKey stable key for the Settings tab.
 */
@Composable
fun rememberFrnkAdaptiveNavTabs(
    homeRoot: NavKey,
    settingsRoot: NavKey,
    middleTabs: List<FrnkAdaptiveNavTab> = emptyList(),
    homeKey: String = "home",
    settingsKey: String = "settings",
): List<FrnkAdaptiveNavTab> {
    val homeLabel = Theme[strings][stringNavHome]
    val settingsLabel = Theme[strings][stringSettings]
    val homeIcon = Theme[icons][iconNavHome]
    val settingsIcon = Theme[icons][iconSettings]

    return remember(
        homeRoot,
        settingsRoot,
        middleTabs,
        homeKey,
        settingsKey,
        homeLabel,
        settingsLabel,
        homeIcon,
        settingsIcon,
    ) {
        buildList {
            add(
                FrnkAdaptiveNavTab(
                    key = homeKey,
                    root = homeRoot,
                    label = homeLabel,
                    icon = homeIcon,
                    iosSystemIcon = "house",
                ),
            )
            addAll(middleTabs)
            add(
                FrnkAdaptiveNavTab(
                    key = settingsKey,
                    root = settingsRoot,
                    label = settingsLabel,
                    icon = settingsIcon,
                    iosSystemIcon = "gearshape",
                ),
            )
        }
    }
}
