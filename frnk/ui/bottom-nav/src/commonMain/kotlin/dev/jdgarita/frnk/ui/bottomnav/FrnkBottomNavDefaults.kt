package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.scaffolds.BottomNavScaffoldState
import dev.jdgarita.frnk.ui.scaffolds.BottomNavTab
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringNavHome
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.strings

/**
 * Builds the default [BottomNavScaffoldState] for [FrnkAdaptiveBottomNavScaffold], enforcing the product
 * contract that **every app has at least Home and Settings**: a fixed **Home** tab, the host's optional
 * [middleTabs] (zero or more), then a fixed **Settings** tab — in that order. Home and Settings resolve
 * their icon + label from `FrnkIcons` / `FrnkStrings` ([iconNavHome] / [stringNavHome] and [iconSettings] /
 * [stringSettings]), so hosts re-skin them through `FrnkThemeConfig` rather than passing them here.
 *
 * Override the middle of the bar by passing [middleTabs]; the Home/Settings bookends are always present.
 * Hosts that need a fully custom shape can build [BottomNavScaffoldState] by hand.
 *
 * @param middleTabs the host's configurable destinations between Home and Settings (e.g. Library, Activity).
 * @param selectedIndex which tab is initially selected (0 = Home … last = Settings).
 * @param homeKey stable key for the Home tab; the host switches on this in `tabContent`.
 * @param settingsKey stable key for the Settings tab.
 */
@Composable
fun rememberFrnkBottomNavState(
    middleTabs: List<BottomNavTab> = emptyList(),
    selectedIndex: Int = 0,
    homeKey: String = "home",
    settingsKey: String = "settings",
): BottomNavScaffoldState {
    val homeLabel = Theme[strings][stringNavHome]
    val settingsLabel = Theme[strings][stringSettings]
    val homeIcon = Theme[icons][iconNavHome]
    val settingsIcon = Theme[icons][iconSettings]

    return remember(middleTabs, selectedIndex, homeKey, settingsKey, homeLabel, settingsLabel, homeIcon, settingsIcon) {
        BottomNavScaffoldState(
            tabs =
                buildList {
                    add(BottomNavTab(key = homeKey, icon = homeIcon, label = homeLabel))
                    addAll(middleTabs)
                    add(BottomNavTab(key = settingsKey, icon = settingsIcon, label = settingsLabel))
                },
            selectedIndex = selectedIndex,
        )
    }
}
