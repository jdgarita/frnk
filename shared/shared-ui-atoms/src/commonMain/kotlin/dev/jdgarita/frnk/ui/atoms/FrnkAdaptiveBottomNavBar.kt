package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorOutlineVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors

/**
 * Which native silhouette the [FrnkAdaptiveBottomNavBar] renders. Chosen explicitly by the host
 * (the toolkit's existing `*Variant` precedent, e.g. `FrnkButtonVariant`) rather than read from
 * `PlatformInfo` — `shared-ui-atoms` is platform-agnostic `commonMain`-only and has no dependency on
 * `shared-utils`, and keeping the choice in state makes the atom previewable/testable per shape. The
 * host derives the per-platform default (see the demo, which uses `PlatformInfo.osName`).
 */
@Immutable
enum class FrnkAdaptiveNavStyle {
    /** Full-width frosted (Haze-blurred) translucent bar pinned to the bottom safe area — iOS feel. */
    IosFrostedBar,

    /** The toolkit's existing centered floating pill (delegates to [FrnkBottomNavBar]) — Android feel. */
    AndroidFloatingPill,
}

/**
 * Platform-adaptive bottom navigation bar (SPIKE: `spike/adaptive-bottom-nav`). Renders the toolkit's
 * floating pill on Android and a full-width Haze-frosted translucent bar on iOS, selected by
 * [FrnkAdaptiveBottomNavBarState.style]. Reuses [FrnkBottomNavItem] verbatim and, for the pill style,
 * delegates to the existing [FrnkBottomNavBar] so there is a single source of the pill geometry/behaviour.
 *
 * **No skeleton, by design** — a bottom nav is persistent navigation chrome, never a content surface that
 * loads (same rationale `FrnkEmptyState` records). The eventual *content* skeletonises; the bar does not.
 *
 * For the frosted (iOS) style the frost samples the pixels behind the bar, so the host marks its
 * scrollable content as the Haze **source** (`Modifier.hazeSource(hazeState)`) and passes the same
 * [hazeState] here. [hazeState] is **nullable**: pass `null` (the default) when you don't want a live
 * blur — the frosted bar then falls back to a flat translucent surface, and the [AndroidFloatingPill]
 * style ignores it entirely (so pill-only hosts never need to construct one). On Android < 12 (no
 * `RenderEffect`) Haze degrades to a flat tint automatically; that fallback is accepted.
 */
@Composable
fun FrnkAdaptiveBottomNavBar(
    state: FrnkAdaptiveBottomNavBarState,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
) {
    when (state.style) {
        FrnkAdaptiveNavStyle.AndroidFloatingPill ->
            FrnkBottomNavBar(
                state =
                    FrnkBottomNavBarState(
                        items = state.items,
                        selectedIndex = state.selectedIndex,
                        enabled = state.enabled,
                    ),
                onItemSelected = onItemSelected,
                modifier = modifier,
            )

        FrnkAdaptiveNavStyle.IosFrostedBar ->
            IosFrostedBar(state = state, onItemSelected = onItemSelected, modifier = modifier, hazeState = hazeState)
    }
}

@Immutable
data class FrnkAdaptiveBottomNavBarState(
    val items: List<FrnkBottomNavItem>,
    val selectedIndex: Int,
    val style: FrnkAdaptiveNavStyle,
    val enabled: Boolean = true,
)

// iOS frosted-bar geometry — exact design metrics, kept as private dp vals (same precedent as the pill's
// constants in FrnkBottomNavBar and FrnkSwitch's TrackWidth). Colors/blur still resolve from Theme tokens.
private val IosBarContentHeight = 56.dp
private val IosIconSize = 24.dp
private val IosItemMinWidth = 56.dp
private val IosHairlineHeight = 1.dp
private val IosFrostBlurRadius = 24.dp

// Translucency of the frost's own surface fill layered over the blurred backdrop. Low enough that the
// content behind stays legibly diffused (the iOS material look), high enough to keep icon contrast.
private const val IOS_FROST_ALPHA = 0.55f

/** Shared layout metrics for [FrnkAdaptiveBottomNavBar], per [FrnkAdaptiveNavStyle]. */
object FrnkAdaptiveBottomNavBarDefaults {
    /** Reserved height for the floating pill — identical to [FrnkBottomNavBarDefaults.BarHeight]. */
    val PillBarHeight: Dp = FrnkBottomNavBarDefaults.BarHeight

    /**
     * Height of the iOS frosted bar's content row (hairline + items), **excluding** the bottom
     * navigation-bar safe-area inset, which the bar adds on top (mirrors `FrnkTopAppBar`'s status-bar
     * handling). This is the raw content metric; prefer [barHeightWithSafeArea] for layout reservations
     * and the collapse-offset distance, since the bar actually occupies this **plus** the inset.
     */
    val IosFrostedContentHeight: Dp = IosBarContentHeight + IosHairlineHeight

    /** The raw (non-inset) content height for the given [style]. Exposed for callers that handle insets themselves. */
    fun barHeight(style: FrnkAdaptiveNavStyle): Dp =
        when (style) {
            FrnkAdaptiveNavStyle.AndroidFloatingPill -> PillBarHeight
            FrnkAdaptiveNavStyle.IosFrostedBar -> IosFrostedContentHeight
        }

    /**
     * The bar's **full rendered height** for the given [style], including the bottom safe-area inset the
     * [IosFrostedBar] consumes via `windowInsetsPadding(navigationBars)`. This is the value to pass to
     * `collapsibleBarOffset` (so the bar fully clears the screen on collapse) and to reserve as scrollable
     * content's bottom inset (so the last item settles above the bar). The floating pill needs no inset, so
     * it returns [PillBarHeight] unchanged.
     */
    @Composable
    fun barHeightWithSafeArea(style: FrnkAdaptiveNavStyle): Dp =
        when (style) {
            FrnkAdaptiveNavStyle.AndroidFloatingPill -> PillBarHeight
            FrnkAdaptiveNavStyle.IosFrostedBar ->
                IosFrostedContentHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        }
}

@Composable
private fun IosFrostedBar(
    state: FrnkAdaptiveBottomNavBarState,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
) {
    val haptics = LocalFrnkHaptics.current
    val selected = state.selectedIndex.coerceIn(0, (state.items.size - 1).coerceAtLeast(0))
    val frostColor = Theme[colors][colorSurface]
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                // Frost: blur the hazeSource content behind the bar + a translucent surface tint over it,
                // so the backdrop diffuses through (the iOS material). When no hazeState is supplied, fall
                // back to a flat translucent surface (no blur). Tokens only — never a hardcoded color. The
                // disabled-state dimming lives on the chrome below, NOT here, so it never dims the backdrop.
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState) {
                            blurEffect {
                                blurRadius = IosFrostBlurRadius
                                colorEffects = listOf(HazeColorEffect.tint(frostColor.copy(alpha = IOS_FROST_ALPHA)))
                                noiseFactor = 0f
                            }
                        }
                    } else {
                        Modifier.background(frostColor.copy(alpha = IOS_FROST_ALPHA))
                    },
                ),
    ) {
        // Dim only the chrome (hairline + items) when disabled — applying alpha to the frost Box above
        // would also dim the sampled/blurred backdrop, producing a translucent content-bleeding bar.
        Column(modifier = Modifier.alpha(if (state.enabled) 1f else 0.4f)) {
            // Top hairline, full-bleed (sits above the items, drawn over the frost).
            Box(Modifier.fillMaxWidth().height(IosHairlineHeight).background(Theme[colors][colorOutlineVariant]))
            Row(
                // The frost fills into the home-indicator area (the Box background extends to the screen
                // edge); the items row is inset above it via navigationBars padding.
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IosBarContentHeight)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.items.forEachIndexed { index, item ->
                    IosFrostedItem(
                        item = item,
                        isSelected = index == selected,
                        enabled = state.enabled,
                        onClick = {
                            if (index != selected) haptics.perform(HapticType.Selection)
                            onItemSelected(index)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun IosFrostedItem(
    item: FrnkBottomNavItem,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    // Selected → colorPrimary; idle → colorOnSurfaceVariant. Crossfade in lock-step (icon + label both read
    // the tint via LocalContentColor), matching the existing pill's interaction language.
    val tint by animateColorAsState(
        targetValue = Theme[colors][if (isSelected) colorPrimary else colorOnSurfaceVariant],
        label = "ios_nav_item_tint",
    )
    Column(
        modifier =
            Modifier
                .widthIn(min = IosItemMinWidth)
                .selectable(
                    selected = isSelected,
                    enabled = enabled,
                    role = Role.Tab,
                    onClick = onClick,
                ).padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ProvideContentColor(tint) {
            FrnkIcon(
                state = FrnkIconState(imageVector = item.icon, contentDescription = item.label, size = IosIconSize),
            )
            // Icon-with-label is the UITabBar idiom; the label reads the same animated content color.
            FrnkText(state = FrnkTextState.BodySmall(text = item.label, color = null, singleLine = true))
        }
    }
}
