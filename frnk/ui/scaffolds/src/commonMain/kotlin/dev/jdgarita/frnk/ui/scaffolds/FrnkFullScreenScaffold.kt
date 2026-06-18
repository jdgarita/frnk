package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconClose
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringClose
import dev.jdgarita.frnk.ui.theme.strings

/** Defaults for [FrnkFullScreenScaffold]. */
object FrnkFullScreenScaffoldDefaults {
    /**
     * The reserved height of the close (✕) button band at the top of the screen. Content is inset
     * below it (mirrors the standard 48dp touch target of `FrnkIconButton`), so top-aligned content
     * never renders behind the close button.
     */
    val CloseButtonHeight: Dp = 48.dp
}

/**
 * The toolkit's **immersive screen template** — a full-bleed surface with **no top bar** and an
 * always-present top-right close (✕) button, the counterpart to [FrnkScreenScaffold] for screens that
 * own the whole window (onboarding, paywalls, full-screen media). The close button respects
 * [WindowInsets.safeDrawing] so it clears the status bar, display cutout, and the navigation gesture
 * area on every platform (on iOS it resolves to the safe-area insets, clearing the notch / home
 * indicator).
 *
 * Like [FrnkScreenScaffold], this is **pure chrome**: it paints the [containerColor] backdrop, hosts
 * the close affordance, and — crucially — hands its [content] slot a **merged [PaddingValues]** that
 * already folds in the safe-drawing insets, the reserved close-button band
 * ([FrnkFullScreenScaffoldDefaults.CloseButtonHeight]), and the caller's own [contentPadding]. Content
 * that applies this padding (e.g. as the `contentPadding` of a scrollable column) starts below the ✕
 * and ends above the home indicator, and a scrolling list scrolls **under** the pinned ✕. It carries
 * **no sealed `*State`** — loading / error visuals belong to [content] (rendered with the atoms' own
 * `Skeleton` / error states), matching the scaffold convention (`docs/HOST_INTEGRATION.md` §9: chrome
 * templates skip the skeleton; their content owns it).
 *
 * The close intent is the caller's: typically a host wires [onCloseClick] to a `UiIntent` so the
 * back-stack pop happens in the ViewModel/effect layer, never inline here.
 *
 * @param onCloseClick invoked when the user taps the ✕.
 * @param containerColor the full-screen backdrop; defaults to `colorBackground`.
 * @param closeContentDescription accessibility label for the ✕; defaults to the `stringClose` token.
 * @param contentPadding extra padding folded into the [PaddingValues] handed to [content], on top of
 *   the safe-drawing insets and the reserved close-button band.
 * @param content the immersive body; receives the merged [PaddingValues] to apply to its content.
 */
@Composable
fun FrnkFullScreenScaffold(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Theme[colors][colorBackground],
    closeContentDescription: String = Theme[strings][stringClose],
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.(contentPadding: PaddingValues) -> Unit
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(containerColor)
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val safe = WindowInsets.safeDrawing.asPaddingValues()
        // Fold safe-area insets + the reserved close-button band + the caller's padding into one set,
        // so the close button and the content can never collide and the body clears every system bar.
        val merged =
            PaddingValues(
                start = safe.calculateStartPadding(layoutDirection) + contentPadding.calculateStartPadding(layoutDirection),
                top =
                    safe.calculateTopPadding() +
                        FrnkFullScreenScaffoldDefaults.CloseButtonHeight +
                        contentPadding.calculateTopPadding(),
                end = safe.calculateEndPadding(layoutDirection) + contentPadding.calculateEndPadding(layoutDirection),
                bottom = safe.calculateBottomPadding() + contentPadding.calculateBottomPadding()
            )

        content(merged)

        FrnkIconButton(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            state =
                FrnkIconButtonState.Content(
                    imageVector = Theme[icons][iconClose],
                    contentDescription = closeContentDescription,
                    tint = colorOnSurfaceVariant
                ),
            onClick = onCloseClick
        )
    }
}