package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.DrawableResource

/**
 * A single bottom-nav tab for [FrnkTabbedNavScaffold] that carries **both** icon forms so one tab list
 * can feed either [FrnkAdaptiveNavEngine] for the side-by-side A/B:
 *  - [icon] — the `ImageVector` the **Calf** bar uses (rasterised on iOS / drawn via Material3 on Android).
 *  - [androidIcon] + [iosSystemIcon] — the resource-based icons the **adaptive-nav-bar** engine requires
 *    (`DrawableResource` on Android, SF-Symbol string on iOS).
 *
 * It is the engine-agnostic sibling of `dev.jdgarita.frnk.ui.nav.FrnkNavTab`: same `key` + [root]
 * back-stack model, but the presentation carries every form a bar engine might need. Build the per-tab
 * back stacks from the same list via `rememberFrnkTabbedBackStacks(tabs = navTabs.map { FrnkTab(it.key, it.root) })`,
 * and let [FrnkTabbedNavScaffold] derive the bar items from it. [rememberFrnkAdaptiveNavTabs] builds the
 * default Home + middle + Settings list with the toolkit's bundled icons.
 */
@Immutable
data class FrnkAdaptiveNavTab(
    val key: String,
    val root: NavKey,
    val label: String,
    val icon: ImageVector,
    val androidIcon: DrawableResource,
    val iosSystemIcon: String,
    val selectedAndroidIcon: DrawableResource? = null,
    val selectedIosSystemIcon: String? = null,
)
