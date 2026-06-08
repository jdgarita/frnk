package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.Res
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.frnk_nav_home
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.frnk_nav_settings
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringNavHome
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.strings

/**
 * Builds the default [FrnkAdaptiveNavTab] list for [FrnkTabbedNavScaffold], enforcing the same product
 * contract as [rememberFrnkBottomNavState] — **every app has at least Home and Settings** — but for the
 * nav3 multiple-back-stack path: a fixed **Home** tab, the host's optional [middleTabs], then a fixed
 * **Settings** tab. This is what gives the bookends "the same treatment" across both bar engines: Home
 * and Settings resolve their `ImageVector` (Calf) from `iconNavHome`/`iconSettings` tokens **and** carry
 * the toolkit's bundled resource icons (`adaptive-nav-bar` engine) + SF-Symbol names, with labels from
 * `stringNavHome`/`stringSettings`. Hosts re-skin them through `FrnkThemeConfig`.
 *
 * The host supplies each bookend's back-stack [homeRoot]/[settingsRoot] (the `NavKey` its tab starts
 * from) since routes are host-defined; middle tabs (with their own icons/roots) slot between them.
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
    val homeIcon = Theme[icons][iconNavHome]
    val settingsIcon = Theme[icons][iconSettings]
    val homeLabel = Theme[strings][stringNavHome]
    val settingsLabel = Theme[strings][stringSettings]

    return remember(
        homeRoot,
        settingsRoot,
        middleTabs,
        homeKey,
        settingsKey,
        homeIcon,
        settingsIcon,
        homeLabel,
        settingsLabel,
    ) {
        buildList {
            add(
                FrnkAdaptiveNavTab(
                    key = homeKey,
                    root = homeRoot,
                    label = homeLabel,
                    icon = homeIcon,
                    androidIcon = Res.drawable.frnk_nav_home,
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
                    androidIcon = Res.drawable.frnk_nav_settings,
                    iosSystemIcon = "gearshape",
                ),
            )
        }
    }
}
