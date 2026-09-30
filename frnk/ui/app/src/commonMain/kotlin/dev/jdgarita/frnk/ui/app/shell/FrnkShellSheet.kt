package dev.jdgarita.frnk.ui.app.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors

/**
 * A modal sheet [FrnkStackShell] presents above its stack, raised and lowered by [key] through
 * [FrnkShellNavigator.present] / [FrnkShellNavigator.dismiss].
 *
 * @param dismissible false keeps the sheet up against every user exit — back, swipe down and an outside
 *   tap (hard gates such as a mandatory paywall); only [FrnkShellNavigator.dismiss] takes it down.
 * @param dismissOnClickOutside whether a tap on the scrim closes a dismissible sheet.
 * @param surfaceColor the sheet's surface, read from the theme at composition.
 * @param content the sheet's body, with the shell's navigator. Each presentation composes it afresh,
 *   with its own ViewModel store.
 */
@Immutable
data class FrnkShellSheet(
    val key: String,
    val dismissible: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val surfaceColor: @Composable () -> Color = { Theme[colors][colorBackground] },
    val content: @Composable (navigator: FrnkShellNavigator) -> Unit
)