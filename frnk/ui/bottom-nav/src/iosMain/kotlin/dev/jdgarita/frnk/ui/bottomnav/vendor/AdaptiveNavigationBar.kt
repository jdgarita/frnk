package dev.jdgarita.frnk.ui.bottomnav.vendor

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.cstr
import kotlinx.cinterop.useContents
import org.jetbrains.compose.resources.painterResource
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.kCACornerCurveContinuous
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIGlassContainerEffect
import platform.UIKit.UIGlassEffect
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIImageView
import platform.UIKit.UIScreen
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UITabBarItemPositioning
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIVisualEffectView
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_setAssociatedObject

/*
 * Vendored iOS implementation of the adaptive bottom bar — see `AdaptiveNavBarModels.kt` for provenance.
 *
 * **Owned & animated.** The upstream library built the whole native bar in the `UIKitView` *factory* and
 * left the `update` block syncing only the selected item — so any FAB toggle or theme change forced the
 * consumer to recreate the whole `UIKitView` (a hard snap, no transition). This vendored version splits the
 * work: the factory builds the static views **once**, and the `update` block drives the changing state —
 * the FAB fading in/out, the tab bar **sliding** between its narrow (FAB-beside) and full-width geometries,
 * and theme-color re-application — all via `UIView.animateWithDuration`. No `key(...)` recreate needed.
 *
 * Simplified vs upstream: the "FAB stacked above a >3-item bar" layout is dropped — the toolkit's bars are
 * small and always render the FAB *beside* the tabs (the bar narrows to make room), which is the layout the
 * slide animates. iOS-26+ renders the native glass `UITabBar`; older iOS falls back to [ComposeNavigationBar].
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    iosFab: IosFabItem? = null,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    colors: AdaptiveNavigationBarColors,
    onIosFabClick: () -> Unit = {},
) {
    val isiOS26OrAbove =
        NSProcessInfo.processInfo
            .operatingSystemVersion
            .useContents {
                majorVersion >= 26
            }

    if (isiOS26OrAbove) {
        IosNavBarVersion26(
            items = items,
            iosFab = iosFab,
            selectedIndex = selectedIndex,
            colors = colors,
            onItemSelected = onItemSelected,
            onIosFabClick = onIosFabClick,
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            iosFab?.let { fabItem ->
                ExtendedFloatingActionButton(
                    modifier =
                        Modifier
                            .animateContentSize()
                            .padding(end = 32.dp, bottom = 24.dp),
                    onClick = onIosFabClick,
                    icon = {
                        Icon(
                            painter = painterResource(fabItem.icon),
                            contentDescription = fabItem.contentDescription,
                        )
                    },
                    text = {
                        Text(text = fabItem.title)
                    },
                    expanded = fabItem.showLabel,
                    containerColor = fabItem.containerColor,
                    contentColor = fabItem.contentColor,
                )
            }
            ComposeNavigationBar(
                windowInsets = windowInsets,
                colors = colors,
                items = items,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
            )
        }
    }
}

/** Duration of the bar's slide + the FAB's fade/scale transitions, in seconds. */
private const val BAR_ANIM_DURATION = 0.28

/** Tracks the last-applied state on a single native bar instance so the `update` block animates only on
 *  real changes (presence flip / theme swap) rather than on every recomposition (e.g. tab selection).
 *  Held in `remember` (see [IosNavBarVersion26]); no longer an `NSObject`. */
private class IosBarState {
    var lastFabPresent: Boolean = false
    var lastColors: AdaptiveNavigationBarColors? = null
}

@OptIn(
    ExperimentalForeignApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
private fun IosNavBarVersion26(
    items: List<NavigationItem>,
    iosFab: IosFabItem?,
    selectedIndex: Int,
    colors: AdaptiveNavigationBarColors,
    onItemSelected: (Int) -> Unit,
    onIosFabClick: () -> Unit,
) {
    // Recreate the native bar only when the *items* change (e.g. switching Mode ①↔② in the Lab, which adds
    // the "+" item) — the `UIKitView` factory runs once, so a changed item set can't otherwise reach it.
    // FAB presence / theme changes leave `items` unchanged, so they fall through to the animated `update`
    // block below instead of recreating.
    key(items) {
        val metrics = remember { computeMetrics(items.size) }

        // Per-instance mutable state, **remembered** so BOTH the factory and the update block close over the
        // same object. (An Obj-C associated object can't be used as the handoff: `.cstr` allocates a fresh
        // key pointer per call and associations are keyed by pointer identity, so the update block's lookup
        // would never match the factory's set — the bug that left iOS un-animated.)
        val barState = remember { IosBarState() }

        // The native delegate / FAB handler are wired once in the factory but must always call the *latest*
        // Compose callbacks (a host may rebind them per screen). rememberUpdatedState keeps them current
        // without rebuilding the native views.
        val latestOnItemSelected by rememberUpdatedState(onItemSelected)
        val latestOnFabClick by rememberUpdatedState(onIosFabClick)

        UIKitView(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(metrics.rootHeightPx.dp),
            properties =
                UIKitInteropProperties(
                    placedAsOverlay = true,
                    interactionMode = UIKitInteropInteractionMode.NonCooperative,
                ),
            factory = {
                val initialPresent = iosFab != null
                val rootView =
                    UIView().apply {
                        backgroundColor = UIColor.clearColor
                        clipsToBounds = false
                        layer.masksToBounds = false
                        setFrame(CGRectMake(0.0, 0.0, metrics.screenWidth, metrics.rootHeightPx))
                    }

                val tabBar = buildTabBar(metrics, items, colors, selectedIndex) { latestOnItemSelected(it) }
                tabBar.setFrame(targetTabBarFrame(metrics, initialPresent))
                rootView.addSubview(tabBar)

                if (initialPresent) {
                    rootView.addSubview(buildFab(metrics, iosFab) { latestOnFabClick() })
                }

                barState.lastFabPresent = initialPresent
                barState.lastColors = colors
                rootView
            },
            update = { rootView ->
                val tabBar =
                    rootView.subviews.firstOrNull { it is UITabBar } as? UITabBar
                        ?: return@UIKitView
                val present = iosFab != null

                // 1. Selection — cheap, every recomposition.
                val current = tabBar.items?.getOrNull(selectedIndex)
                if (tabBar.selectedItem != current) {
                    tabBar.selectedItem = current as UITabBarItem?
                }

                // 2. Theme colors — re-apply the native appearance (no recreate) when they actually change.
                //    Rebuild the FAB too so its glass tint / icon color track the palette.
                if (barState.lastColors != colors) {
                    barState.lastColors = colors
                    applyTabBarColors(tabBar, colors)
                    if (present) {
                        existingFab(rootView)?.removeFromSuperview()
                        rootView.addSubview(buildFab(metrics, iosFab) { latestOnFabClick() })
                    }
                }

                // 3. Primary-action presence — animate the FAB fade + the bar slide on a real flip.
                if (barState.lastFabPresent != present) {
                    barState.lastFabPresent = present
                    val existing = existingFab(rootView)
                    if (present && existing == null) {
                        val fab =
                            buildFab(metrics, iosFab) { latestOnFabClick() }.apply {
                                alpha = 0.0
                                transform = CGAffineTransformMakeScale(0.6, 0.6)
                            }
                        rootView.addSubview(fab)
                        UIView.animateWithDuration(
                            duration = BAR_ANIM_DURATION,
                            animations = {
                                fab.alpha = 1.0
                                fab.transform = CGAffineTransformMakeScale(1.0, 1.0)
                            },
                        )
                    } else if (!present && existing != null) {
                        UIView.animateWithDuration(
                            duration = BAR_ANIM_DURATION,
                            animations = {
                                existing.alpha = 0.0
                                existing.transform = CGAffineTransformMakeScale(0.6, 0.6)
                            },
                            completion = { _ -> existing.removeFromSuperview() },
                        )
                    }
                    UIView.animateWithDuration(
                        duration = BAR_ANIM_DURATION,
                        animations = { tabBar.setFrame(targetTabBarFrame(metrics, present)) },
                    )
                }
            },
        )
    }
}

/** Resolved geometry for the native bar. All `Double`s are points; the bar always renders the FAB beside
 *  the tabs (the bar narrows when the FAB is present), which is what the slide animates between. */
private class IosBarMetrics(
    val screenWidth: Double,
    val rootHeightPx: Double,
    val tabBarHeight: Double,
    val tabBarY: Double,
    val tabItemWidth: Double,
    val fabSize: Double,
    val fabX: Double,
    val fabY: Double,
    val narrowWidth: Double,
)

@OptIn(ExperimentalForeignApi::class)
private fun computeMetrics(itemCount: Int): IosBarMetrics {
    val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
    val tabBarHeight = 68.0
    val fabSize = 64.0
    val endPadding = 20.0
    val bottomPadding = 20.0
    val rootHeightPx = tabBarHeight + (bottomPadding * 2)
    val tabBarY = rootHeightPx - bottomPadding - tabBarHeight
    val fabX = screenWidth - endPadding - fabSize
    val fabY = tabBarY - 2
    val tabItemWidth =
        when {
            itemCount > 3 -> 106.0
            else -> {
                val maxWidth = 140.0
                val minWidth = 120.0
                val step = (maxWidth - minWidth) / (3 - 1).toDouble()
                maxWidth - ((itemCount - 1) * step)
            }
        }
    val narrowWidth = (itemCount * tabItemWidth).coerceAtMost(fabX - endPadding)
    return IosBarMetrics(
        screenWidth = screenWidth,
        rootHeightPx = rootHeightPx,
        tabBarHeight = tabBarHeight,
        tabBarY = tabBarY,
        tabItemWidth = tabItemWidth,
        fabSize = fabSize,
        fabX = fabX,
        fabY = fabY,
        narrowWidth = narrowWidth,
    )
}

/** Full-width (no FAB) vs narrow-left (FAB beside) — the two geometries the bar slides between. */
@OptIn(ExperimentalForeignApi::class)
private fun targetTabBarFrame(
    metrics: IosBarMetrics,
    fabPresent: Boolean,
) = CGRectMake(
    x = 0.0,
    y = metrics.tabBarY,
    width = if (fabPresent) metrics.narrowWidth else metrics.screenWidth,
    height = metrics.tabBarHeight,
)

@OptIn(ExperimentalForeignApi::class)
private fun existingFab(rootView: UIView): UIVisualEffectView? =
    rootView.subviews.firstOrNull { it is UIVisualEffectView } as? UIVisualEffectView

@OptIn(ExperimentalForeignApi::class)
private fun buildTabBar(
    metrics: IosBarMetrics,
    items: List<NavigationItem>,
    colors: AdaptiveNavigationBarColors,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
): UITabBar =
    UITabBar().apply {
        translucent = true
        opaque = false
        backgroundImage = UIImage()
        shadowImage = UIImage()
        backgroundColor = UIColor.clearColor
        clipsToBounds = false
        layer.masksToBounds = false
        layer.cornerRadius = 30.0
        layer.cornerCurve = kCACornerCurveContinuous

        applyTabBarColors(this, colors)

        val nativeItems =
            items.mapIndexed { index, item ->
                val normalImage =
                    (
                        UIImage.systemImageNamed(item.systemIcon)
                            ?: UIImage.imageNamed(item.systemIcon)
                    )?.imageWithRenderingMode(
                        UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate,
                    )

                val selectedImage =
                    (
                        item.selectedSystemIcon?.let {
                            UIImage.systemImageNamed(it) ?: UIImage.imageNamed(it)
                        } ?: normalImage
                    )?.imageWithRenderingMode(
                        UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate,
                    )

                UITabBarItem(
                    title = if (item.showLabel) item.title else null,
                    image = normalImage,
                    selectedImage = selectedImage,
                ).apply {
                    tag = index.toLong()
                    // No fixed `itemWidth`: the bar uses Fill positioning (see applyTabBarColors) so items
                    // distribute evenly across the *current* bar width. A fixed width overflowed the narrow
                    // (FAB-present) bar and collapsed the stacked icon/label on top of each other.
                    enabled = item.enabled
                    badgeValue = item.badge
                    badgeColor = colors.badgeContainerColor.toUIColor()
                    setBadgeTextAttributes(
                        mapOf(
                            NSForegroundColorAttributeName to colors.badgeContentColor.toUIColor(),
                        ),
                        UIControlStateNormal,
                    )
                }
            }

        setItems(nativeItems, animated = false)
        selectedItem = nativeItems.getOrNull(selectedIndex)
        layoutMargins = UIEdgeInsetsMake(0.0, 0.0, 0.0, 0.0)

        val delegate = TabBarDelegate(onItemSelected)
        this.delegate = delegate
        objc_setAssociatedObject(this, "tabBarDelegate".cstr, delegate, OBJC_ASSOCIATION_RETAIN_NONATOMIC)
    }

@OptIn(ExperimentalForeignApi::class)
private fun applyTabBarColors(
    tabBar: UITabBar,
    colors: AdaptiveNavigationBarColors,
) {
    val appearance =
        UITabBarAppearance().apply {
            configureWithTransparentBackground()

            backgroundEffect =
                UIBlurEffect.effectWithStyle(
                    UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial,
                )

            backgroundColor =
                colors.containerColor
                    .toUIColor()
                    .colorWithAlphaComponent(0.72)

            shadowColor = UIColor.clearColor

            stackedLayoutAppearance.selected.titleTextAttributes =
                mapOf(
                    NSForegroundColorAttributeName to colors.selectedTextColor.toUIColor(),
                )
            stackedLayoutAppearance.selected.iconColor = colors.selectedIconColor.toUIColor()
            stackedLayoutAppearance.normal.iconColor = colors.unselectedIconColor.toUIColor()
            stackedLayoutAppearance.selected.badgeBackgroundColor = colors.badgeContainerColor.toUIColor()
        }
    tabBar.standardAppearance = appearance
    tabBar.scrollEdgeAppearance = appearance
    // `itemPositioning` is a UITabBar property (not an appearance one). Fill (not the upstream Centered)
    // distributes items evenly across the current bar width, so the stacked icon/label never overflows /
    // overlaps when the bar is narrow (FAB present), and items redistribute as the bar slides.
    tabBar.itemPositioning = UITabBarItemPositioning.UITabBarItemPositioningFill
}

/** Builds the glass FAB (a `UIVisualEffectView` glass container) positioned at the bar's right edge. */
@OptIn(ExperimentalForeignApi::class)
private fun buildFab(
    metrics: IosBarMetrics,
    iosFab: IosFabItem,
    onFabClick: () -> Unit,
): UIVisualEffectView {
    val glassContainer =
        UIVisualEffectView(effect = UIGlassContainerEffect()).apply {
            backgroundColor = UIColor.clearColor
            clipsToBounds = false
            layer.masksToBounds = false
            setFrame(CGRectMake(metrics.fabX, metrics.fabY, metrics.fabSize, metrics.fabSize))
            userInteractionEnabled = true
        }

    val glassEffect =
        UIGlassEffect().apply {
            tintColor = iosFab.containerColor.toUIColor().colorWithAlphaComponent(0.72)
            interactive = true
        }

    val fabEffectView =
        UIVisualEffectView(effect = glassEffect).apply {
            setFrame(CGRectMake(0.0, 0.0, metrics.fabSize, metrics.fabSize)) // relative to glassContainer
            layer.cornerRadius = metrics.fabSize / 2.0
            layer.cornerCurve = kCACornerCurveContinuous
            clipsToBounds = true
        }

    val fabIcon =
        (
            UIImage.systemImageNamed(iosFab.systemIcon)
                ?: UIImage.imageNamed(iosFab.systemIcon)
        )?.imageWithRenderingMode(
            UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate,
        )

    val iconView =
        UIImageView(image = fabIcon).apply {
            tintColor = iosFab.contentColor.toUIColor()
            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
            val iconSize = 24.0
            val inset = (metrics.fabSize - iconSize) / 2.0
            setFrame(CGRectMake(inset, inset, iconSize, iconSize))
        }
    fabEffectView.contentView.addSubview(iconView)

    val tapButton = UIButton.buttonWithType(UIButtonTypeSystem)
    tapButton.setFrame(CGRectMake(0.0, 0.0, metrics.fabSize, metrics.fabSize))
    tapButton.backgroundColor = UIColor.clearColor

    val fabHandler = FabHandler(onFabClick)
    tapButton.addTarget(fabHandler, NSSelectorFromString("onFabTap"), UIControlEventTouchUpInside)
    // Retain the handler for the FAB's lifetime (released with the glass container on removal).
    objc_setAssociatedObject(glassContainer, "fabHandler".cstr, fabHandler, OBJC_ASSOCIATION_RETAIN_NONATOMIC)

    fabEffectView.contentView.addSubview(tapButton)
    glassContainer.contentView.addSubview(fabEffectView)
    return glassContainer
}

private class FabHandler(
    val onClick: () -> Unit,
) : NSObject() {
    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun onFabTap() {
        onClick()
    }
}

internal fun Color.toUIColor(): UIColor =
    UIColor(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )

private class TabBarDelegate(
    val onItemSelected: (Int) -> Unit,
) : NSObject(),
    UITabBarDelegateProtocol {
    override fun tabBar(
        tabBar: UITabBar,
        didSelectItem: UITabBarItem,
    ) {
        onItemSelected(didSelectItem.tag.toInt())
    }
}

/**
 * Older-iOS (`< 26`) fallback: a Material3 [NavigationBar]. The glass `UITabBar` above is iOS-26-only;
 * this keeps the bar usable on earlier versions. Material3 is allowed here — `:ui-bottom-nav` is the
 * toolkit's sole Material3 module.
 */
@Composable
private fun ComposeNavigationBar(
    windowInsets: WindowInsets,
    colors: AdaptiveNavigationBarColors,
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    NavigationBar(
        windowInsets = windowInsets,
        containerColor = colors.containerColor,
    ) {
        items.forEachIndexed { index, item ->
            val selected = selectedIndex == index
            NavigationBarItem(
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.selectedIconColor,
                        selectedTextColor = colors.selectedTextColor,
                        unselectedIconColor = colors.unselectedIconColor,
                        unselectedTextColor = colors.unselectedTextColor,
                        indicatorColor = colors.indicatorColor,
                    ),
                selected = selected,
                enabled = item.enabled,
                onClick = {
                    onItemSelected(index)
                },
                icon = {
                    BadgedBox(
                        badge = {
                            when {
                                item.badge != null -> {
                                    Badge(
                                        modifier =
                                            Modifier.offset(
                                                x = 8.dp,
                                                y = (-2).dp,
                                            ),
                                        containerColor = colors.badgeContainerColor,
                                        contentColor = colors.badgeContentColor,
                                    ) {
                                        Text(item.badge)
                                    }
                                }

                                item.showBadgeDot -> {
                                    Badge(
                                        modifier =
                                            Modifier.offset(
                                                x = 4.dp,
                                                y = (-2).dp,
                                            ),
                                        containerColor = colors.badgeContainerColor,
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    when {
                                        selected && item.selectedIcon != null ->
                                            item.selectedIcon

                                        else ->
                                            item.icon
                                    },
                                ),
                            contentDescription =
                                item.contentDescription ?: item.title,
                        )
                    }
                },
                label = {
                    if (item.showLabel) {
                        Text(item.title)
                    }
                },
                alwaysShowLabel = item.showLabel,
            )
        }
    }
}
