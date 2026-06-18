package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * A single tab in the toolkit's fixed `Home · feature · Settings` bottom bar. It carries the two icon
 * forms the adaptive bar needs — [icon] (a Compose [ImageVector] for the Android floating toolbar) and
 * [iosSystemIcon] (an SF-Symbol string for the iOS native bar) — alongside the tab's back-stack [root].
 *
 * Modelled as a `sealed class` with one subtype per fixed slot ([Home], [Feature], [Settings]) so the
 * three-tab shape is expressed in the type system rather than as a bare list. It is the presentation
 * sibling of `dev.jdgarita.frnk.ui.nav.FrnkNavTab`: same `key` + [root] back-stack model, but with the
 * bar's icon forms. [FrnkTabbedNavScaffold] derives the per-tab back stacks from the tab list
 * (`tabs.map { FrnkTab(it.key, it.root) }`) and maps the tabs to bar items; [rememberFrnkBottomNavState]
 * builds the fixed Home + feature + Settings tabs (as a [FrnkBottomNavState]) with the toolkit's theme
 * icons.
 *
 * @property key the stable tab key backing its back stack + selection.
 * @property root the tab's back-stack root destination.
 * @property label the tab label (doubles as the bar item's accessibility description).
 * @property icon the Android `ImageVector` icon.
 * @property iosSystemIcon the iOS SF-Symbol name.
 */
@Immutable
sealed class FrnkBottomNavTab(
    open val key: String,
    open val root: NavKey,
    open val label: String,
    open val icon: ImageVector,
    open val iosSystemIcon: String
) {
    /** The fixed leading Home tab (toolkit-owned; built from theme tokens). */
    @Immutable
    data class Home(
        override val key: String,
        override val root: NavKey,
        override val label: String,
        override val icon: ImageVector,
        override val iosSystemIcon: String
    ) : FrnkBottomNavTab(key, root, label, icon, iosSystemIcon)

    /** The host's configurable center tab (shaped by a [FrnkFeatureItem]). */
    @Immutable
    data class Feature(
        override val key: String,
        override val root: NavKey,
        override val label: String,
        override val icon: ImageVector,
        override val iosSystemIcon: String
    ) : FrnkBottomNavTab(key, root, label, icon, iosSystemIcon)

    /** The fixed trailing Settings tab (toolkit-owned; built from theme tokens). */
    @Immutable
    data class Settings(
        override val key: String,
        override val root: NavKey,
        override val label: String,
        override val icon: ImageVector,
        override val iosSystemIcon: String
    ) : FrnkBottomNavTab(key, root, label, icon, iosSystemIcon)
}