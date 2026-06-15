package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.iconNavAdd
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringPrimaryAction
import dev.jdgarita.frnk.ui.theme.strings

/**
 * The adaptive bar's built-in **primary-action item** — frnk-owned and given the same first-class
 * treatment as the Home/Settings bookends: a default [icon] (the `iconNavAdd` theme token, a plus glyph) +
 * [iosSystemIcon] (`"plus"`, for the iOS native bar) + [label] (the [stringPrimaryAction] token,
 * host-overridable via `FrnkThemeConfig.stringOverrides`). [FrnkTabbedNavScaffold] injects it as a permanent
 * **centered bar item** (Mode B) on both platforms; it shows only when an action is wired.
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
    val icon: ImageVector,
    val iosSystemIcon: String,
    val label: String,
)

/**
 * Builds the toolkit-default [FrnkNavPrimaryAction] from the `iconNavAdd` theme icon and the
 * [stringPrimaryAction] label. Override any field to re-skin the primary-action button per screen while
 * keeping the rest of the defaults.
 */
@Composable
fun rememberFrnkNavPrimaryAction(
    icon: ImageVector = Theme[icons][iconNavAdd],
    iosSystemIcon: String = "plus",
    label: String = Theme[strings][stringPrimaryAction],
): FrnkNavPrimaryAction =
    remember(icon, iosSystemIcon, label) {
        FrnkNavPrimaryAction(icon = icon, iosSystemIcon = iosSystemIcon, label = label)
    }
