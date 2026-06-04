package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBar
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarDefaults
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import kotlin.math.roundToInt

/**
 * The standard screen template: a [FrnkTopAppBar] floating over a single edge-to-edge scrollable
 * content area, with both the top bar and the host's floating bottom bar collapsing on scroll.
 *
 * Content draws **behind** the top bar (and behind the floating bottom bar, via [bottomInset]) so a
 * long list scrolls visibly under both. The [content] slot is handed a [PaddingValues] that already
 * accounts for the top bar's full height (status-bar inset + bar) and [bottomInset]; apply it to the
 * scroll container so the first and last items rest clear of the bars at rest.
 *
 * Collapse is coordinated by [collapsibleBars]: this scaffold registers its
 * [CollapsibleBarsState.nestedScrollConnection] above [content] and translates the top bar up by its
 * height as the fraction grows. The host applies the same fraction to its bottom bar (see
 * `BottomNavScaffoldContent`'s `collapsibleBars` parameter) so the two move together. Pass the **same**
 * [CollapsibleBarsState] instance to every screen sharing one bottom bar, and call
 * [CollapsibleBarsState.reset] when switching screens.
 *
 * This is a layout scaffold (no MVI/ViewModel) — the top bar's title/actions/search are driven purely
 * by [topBar] and its callbacks, which mirror [FrnkTopAppBar].
 *
 * [containerColor] is painted behind the whole screen (the content draws over it), so every screen
 * built on this template follows the active light/dark palette without each host painting its own
 * background. Defaults to `Theme[colors][colorBackground]`; override (e.g. `colorSurface`, or
 * `Color.Transparent` when the host paints its own backdrop behind the scaffold).
 */
@Composable
fun FrnkScreenScaffold(
    topBar: FrnkTopAppBarState,
    collapsibleBars: CollapsibleBarsState,
    modifier: Modifier = Modifier,
    containerColor: Color = Theme[colors][colorBackground],
    bottomInset: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(FrnkSpacing.lg),
    onNavigationClick: () -> Unit = {},
    onActionClick: (FrnkTopAppBarAction) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // Top bar's full rendered height = the content row + the status-bar inset it pads itself with.
    val statusBarTopPx = WindowInsets.statusBars.getTop(density)
    val barHeightPx = with(density) { FrnkTopAppBarDefaults.BarHeight.toPx() } + statusBarTopPx
    val barHeightDp = with(density) { barHeightPx.toDp() }

    // Drive the shared collapse distance off the top bar height so ~one bar-height of scroll toggles.
    LaunchedEffect(barHeightPx) { collapsibleBars.setCollapseDistance(barHeightPx) }

    val mergedPadding =
        PaddingValues(
            start = contentPadding.calculateLeftPadding(layoutDirection),
            top = contentPadding.calculateTopPadding() + barHeightDp,
            end = contentPadding.calculateRightPadding(layoutDirection),
            bottom = contentPadding.calculateBottomPadding() + bottomInset,
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(containerColor)
                .nestedScroll(collapsibleBars.nestedScrollConnection),
    ) {
        content(mergedPadding)

        FrnkTopAppBar(
            state = topBar,
            onNavigationClick = onNavigationClick,
            onActionClick = onActionClick,
            onSearchQueryChange = onSearchQueryChange,
            onSearchClose = onSearchClose,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(x = 0, y = -(collapsibleBars.collapseFraction * barHeightPx).roundToInt()) },
        )
    }
}
