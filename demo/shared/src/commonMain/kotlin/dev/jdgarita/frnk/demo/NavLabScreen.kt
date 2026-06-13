package dev.jdgarita.frnk.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControl
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControlState
import dev.jdgarita.frnk.ui.atoms.FrnkSwitch
import dev.jdgarita.frnk.ui.atoms.FrnkSwitchState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.bottomnav.FrnkBottomFloatingBar
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavBarItem
import dev.jdgarita.frnk.ui.bottomnav.rememberFrnkNavPrimaryAction
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconNavAdd
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

private const val ADD_KEY = "add"

/**
 * **Bottom Nav Lab** — a self-contained harness for comparing the adaptive bar's two primary-action
 * behaviours, so the choice can be judged on-device:
 *  - **Mode ①** keeps a distinct primary-action FAB; the "Primary action" switch toggles it present/absent
 *    (simulating Home↔Settings). The bar **animates** — FAB fades, and on iOS the bar slides narrow↔full.
 *  - **Mode ②** demotes the action to a permanent centered "+" item; the bar never moves.
 *
 * It mounts the real [FrnkBottomFloatingBar] over placeholder content; all state is hoisted into
 * [DemoViewModel] (`navLabMode` / `navLabPrimaryOn` / `navLabSelectedKey`).
 */
@Composable
fun NavLabScreen(
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
    onBack: () -> Unit,
) {
    val homeIcon = Theme[icons][iconNavHome]
    val componentsIcon = Lucide.Component
    val settingsIcon = Theme[icons][iconSettings]
    val addIcon = Theme[icons][iconNavAdd]

    // Mode ①: the three real tabs (FAB is separate). Mode ②: same tabs + a centered "+" action item.
    val itemsMode1 =
        remember(homeIcon, componentsIcon, settingsIcon) {
            listOf(
                FrnkNavBarItem("home", homeIcon, "house", "Home"),
                FrnkNavBarItem("components", componentsIcon, "square.grid.2x2", "Components"),
                FrnkNavBarItem("settings", settingsIcon, "gearshape", "Settings"),
            )
        }
    val itemsMode2 =
        remember(homeIcon, componentsIcon, addIcon, settingsIcon) {
            listOf(
                FrnkNavBarItem("home", homeIcon, "house", "Home"),
                FrnkNavBarItem("components", componentsIcon, "square.grid.2x2", "Components"),
                FrnkNavBarItem(ADD_KEY, addIcon, "plus", "Add"),
                FrnkNavBarItem("settings", settingsIcon, "gearshape", "Settings"),
            )
        }

    val isModeOne = state.navLabMode == 0
    val items = if (isModeOne) itemsMode1 else itemsMode2
    val selectedIndex = items.indexOfFirst { it.key == state.navLabSelectedKey }.coerceAtLeast(0)

    val onItemSelected: (Int) -> Unit = { index ->
        val key = items[index].key
        if (key == ADD_KEY) onIntent(DemoIntent.NavLabActionTapped) else onIntent(DemoIntent.NavLabTabSelected(key))
    }

    // Mode ①'s FAB is wired only when the toggle is on; Mode ② never uses a FAB (the "+" is a bar item).
    val showFab = isModeOne && state.navLabPrimaryOn
    val primaryAction = if (showFab) rememberFrnkNavPrimaryAction() else null
    val onPrimaryAction: (() -> Unit)? = if (showFab) ({ onIntent(DemoIntent.NavLabActionTapped) }) else null

    Box(modifier = Modifier.fillMaxSize()) {
        FrnkScreenScaffold(
            topBar =
                FrnkTopAppBarState(
                    title = "Bottom Nav Lab",
                    navigationIcon = Theme[icons][iconBack],
                    navigationContentDescription = "Back",
                ),
            onNavigationClick = onBack,
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = FrnkSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
            ) {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                "Compare the two primary-action behaviours, then pick one. The bar at the " +
                                    "foot of this screen is the real FrnkBottomFloatingBar.",
                            color = colorOnSurfaceVariant,
                        ),
                )

                FrnkText(state = FrnkTextState.TitleMedium(text = "Behaviour"))
                FrnkSegmentedControl(
                    state =
                        FrnkSegmentedControlState.Content(
                            options = listOf("① Animated FAB", "② Centered item"),
                            selectedIndex = state.navLabMode,
                        ),
                    onOptionSelected = { onIntent(DemoIntent.NavLabModeChanged(it)) },
                )

                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                if (isModeOne) {
                                    "Mode ① — a distinct primary-action FAB. Toggle it below to watch the FAB " +
                                        "fade and (on iOS) the bar slide narrow↔full-width. No jump."
                                } else {
                                    "Mode ② — the action is a permanent centered \"+\" item. The bar never " +
                                        "moves, so no animation is needed. Tap \"+\" to fire the action."
                                },
                            color = colorOnSurfaceVariant,
                        ),
                )

                FrnkText(state = FrnkTextState.TitleMedium(text = "Primary action (Mode ① only)"))
                FrnkSwitch(
                    state =
                        FrnkSwitchState.Content(
                            checked = state.navLabPrimaryOn,
                            enabled = isModeOne,
                        ),
                    onCheckedChange = { onIntent(DemoIntent.NavLabPrimaryToggled(it)) },
                )
                FrnkText(
                    state =
                        FrnkTextState.BodySmall(
                            text = "On ≈ Home (FAB shown) · Off ≈ Settings (FAB hidden)",
                            color = colorOnSurfaceVariant,
                        ),
                )
            }
        }

        FrnkBottomFloatingBar(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            // No extra padding — the bar owns its own bottom margin, so it renders identically here and in
            // FrnkTabbedNavScaffold (the Home screen).
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            primaryAction = primaryAction,
            onPrimaryAction = onPrimaryAction,
        )
    }
}
