package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.Res
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.frnk_nav_primary_action
import dev.jdgarita.frnk.ui.theme.stringPrimaryAction
import dev.jdgarita.frnk.ui.theme.strings
import org.jetbrains.compose.resources.DrawableResource

/**
 * The adaptive bar's built-in **primary-action button** — frnk-owned and given the same first-class
 * treatment as the Home/Settings bookends: a default [androidIcon] (the bundled plus drawable) +
 * [iosSystemIcon] (`"plus"`) + [label] (the [stringPrimaryAction] token, host-overridable via
 * `FrnkThemeConfig.stringOverrides`). The button renders as a FAB on Android and an inline button beside
 * the items on iOS (see [FrnkAdaptiveNavBarBottomBar]); it shows only on
 * [FrnkAdaptiveNavEngine.AdaptiveNavBar].
 *
 * It is the **primary action** of the current surface, not a fixed "add" — a host wires it per screen
 * (create on one tab, compose on another, scan on a third). Re-skin the icon/label per context by passing
 * a custom instance to [FrnkTabbedNavScaffold], and supply the matching behaviour through `onPrimaryAction`.
 *
 * Like every toolkit `@Immutable` state, it carries **no lambda** — the host supplies the `onPrimaryAction`
 * callback separately to [FrnkTabbedNavScaffold], which is what makes the button appear at all (the host
 * decides what tapping it does).
 */
@Immutable
data class FrnkNavPrimaryAction(
    val androidIcon: DrawableResource,
    val iosSystemIcon: String,
    val label: String,
)

/**
 * Builds the toolkit-default [FrnkNavPrimaryAction] from the bundled plus icon and the
 * [stringPrimaryAction] label. Override any field to re-skin the primary-action button per screen while
 * keeping the rest of the defaults.
 */
@Composable
fun rememberFrnkNavPrimaryAction(
    androidIcon: DrawableResource = Res.drawable.frnk_nav_primary_action,
    iosSystemIcon: String = "plus",
    label: String = Theme[strings][stringPrimaryAction],
): FrnkNavPrimaryAction =
    remember(androidIcon, iosSystemIcon, label) {
        FrnkNavPrimaryAction(androidIcon = androidIcon, iosSystemIcon = iosSystemIcon, label = label)
    }
