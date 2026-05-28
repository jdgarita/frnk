package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringNavHome
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.strings

/**
 * Builds the toolkit's standard 3-tab [BottomNavScaffoldState]: a fixed **Home** tab, the
 * host-supplied [middleTab], and a fixed **Settings** tab — in that order. Home and Settings resolve
 * their icon + label from `FrnkIcons` / `FrnkStrings` ([iconNavHome] / [stringNavHome] and
 * [iconSettings] / [stringSettings]), so hosts re-skin them through `FrnkThemeConfig` rather than
 * passing them here.
 *
 * This is the convenience entry point for the product contract ("always Home + configurable middle +
 * Settings"). Hosts that need a different shape can build [BottomNavScaffoldState] by hand instead.
 *
 * @param middleTab the configurable centre destination (e.g. Stats, Library, Activity).
 * @param selectedIndex which of the three tabs is initially selected (0 = Home, 1 = middle, 2 = Settings).
 * @param homeKey stable key for the Home tab; the host switches on this in `tabContent`.
 * @param settingsKey stable key for the Settings tab.
 */
@Composable
fun rememberBottomNavScaffoldState(
    middleTab: BottomNavTab,
    selectedIndex: Int = 0,
    homeKey: String = "home",
    settingsKey: String = "settings",
): BottomNavScaffoldState {
    val homeLabel = Theme[strings][stringNavHome]
    val settingsLabel = Theme[strings][stringSettings]
    val homeIcon = Theme[icons][iconNavHome]
    val settingsIcon = Theme[icons][iconSettings]

    return remember(
        middleTab,
        selectedIndex,
        homeKey,
        settingsKey,
        homeLabel,
        settingsLabel,
        homeIcon,
        settingsIcon,
    ) {
        BottomNavScaffoldState(
            tabs =
                listOf(
                    BottomNavTab(key = homeKey, icon = homeIcon, label = homeLabel),
                    middleTab,
                    BottomNavTab(key = settingsKey, icon = settingsIcon, label = settingsLabel),
                ),
            selectedIndex = selectedIndex,
        )
    }
}
