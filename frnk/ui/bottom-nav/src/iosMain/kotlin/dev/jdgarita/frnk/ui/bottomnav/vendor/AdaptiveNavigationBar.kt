package dev.jdgarita.frnk.ui.bottomnav.vendor

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.useContents
import org.jetbrains.compose.resources.painterResource
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSProcessInfo
import platform.QuartzCore.kCACornerCurveContinuous
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIColor
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIScreen
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UITabBarItemPositioning
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_setAssociatedObject

/*
 * Vendored iOS implementation of the adaptive bottom bar — see `AdaptiveNavBarModels.kt` for provenance.
 *
 * **Owned & themed in place.** The upstream library built the whole native bar in the `UIKitView` *factory*
 * and left the `update` block syncing only the selected item — so any theme change forced the consumer to
 * recreate the whole `UIKitView` (a hard snap). This vendored version splits the work: the factory builds the
 * static views **once**, and the `update` block drives the changing state — selection sync and theme-color
 * re-application — without a recreate.
 *
 * The bar is **full-width and static** (no FAB): the toolkit's primary action is a permanent centered nav
 * item (Mode B), not a separate FAB. iOS-26+ renders the native glass `UITabBar`; older iOS falls back to
 * [ComposeNavigationBar].
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    colors: AdaptiveNavigationBarColors
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
            selectedIndex = selectedIndex,
            colors = colors,
            onItemSelected = onItemSelected
        )
    } else {
        ComposeNavigationBar(
            windowInsets = windowInsets,
            colors = colors,
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected
        )
    }
}

/** Tracks the last-applied colors on a single native bar instance so the `update` block re-applies the
 *  appearance only on a real theme swap (light↔dark) rather than on every recomposition (e.g. tab
 *  selection). Held in `remember` (see [IosNavBarVersion26]). */
private class IosBarState {
    var lastColors: AdaptiveNavigationBarColors? = null
}

@OptIn(
    ExperimentalForeignApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
private fun IosNavBarVersion26(
    items: List<NavigationItem>,
    selectedIndex: Int,
    colors: AdaptiveNavigationBarColors,
    onItemSelected: (Int) -> Unit
) {
    // Recreate the native bar only when the *items* change — the `UIKitView` factory runs once, so a changed
    // item set can't otherwise reach it. Theme changes leave `items` unchanged, so they fall through to the
    // animated `update` block below instead of recreating.
    key(items) {
        val metrics = remember { computeMetrics() }

        // Per-instance mutable state, **remembered** so BOTH the factory and the update block close over the
        // same object.
        val barState = remember { IosBarState() }

        // The native delegate is wired once in the factory but must always call the *latest* Compose callback
        // (a host may rebind it per screen). rememberUpdatedState keeps it current without rebuilding views.
        val latestOnItemSelected by rememberUpdatedState(onItemSelected)

        UIKitView(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(metrics.rootHeightPx.dp),
            properties =
                UIKitInteropProperties(
                    placedAsOverlay = true,
                    interactionMode = UIKitInteropInteractionMode.NonCooperative
                ),
            factory = {
                val rootView =
                    UIView().apply {
                        backgroundColor = UIColor.clearColor
                        clipsToBounds = false
                        layer.masksToBounds = false
                        setFrame(CGRectMake(0.0, 0.0, metrics.screenWidth, metrics.rootHeightPx))
                    }

                val tabBar = buildTabBar(items, colors, selectedIndex) { latestOnItemSelected(it) }
                tabBar.setFrame(tabBarFrame(metrics))
                rootView.addSubview(tabBar)

                barState.lastColors = colors
                rootView
            },
            update = { rootView ->
                val tabBar =
                    rootView.subviews.firstOrNull { it is UITabBar } as? UITabBar
                        ?: return@UIKitView

                // 1. Selection — cheap, every recomposition.
                val current = tabBar.items?.getOrNull(selectedIndex)
                if (tabBar.selectedItem != current) {
                    tabBar.selectedItem = current as UITabBarItem?
                }

                // 2. Theme colors — re-apply the native appearance (no recreate) when they actually change.
                if (barState.lastColors != colors) {
                    barState.lastColors = colors
                    applyTabBarColors(tabBar, colors)
                }
            }
        )
    }
}

/** Resolved geometry for the native full-width bar. All `Double`s are points. */
private class IosBarMetrics(
    val screenWidth: Double,
    val rootHeightPx: Double,
    val tabBarHeight: Double,
    val tabBarY: Double
)

@OptIn(ExperimentalForeignApi::class)
private fun computeMetrics(): IosBarMetrics {
    val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
    val tabBarHeight = 68.0
    val bottomPadding = 20.0
    val rootHeightPx = tabBarHeight + (bottomPadding * 2)
    val tabBarY = rootHeightPx - bottomPadding - tabBarHeight
    return IosBarMetrics(
        screenWidth = screenWidth,
        rootHeightPx = rootHeightPx,
        tabBarHeight = tabBarHeight,
        tabBarY = tabBarY
    )
}

/** The bar's single full-width frame. */
@OptIn(ExperimentalForeignApi::class)
private fun tabBarFrame(metrics: IosBarMetrics) =
    CGRectMake(
        x = 0.0,
        y = metrics.tabBarY,
        width = metrics.screenWidth,
        height = metrics.tabBarHeight
    )

@OptIn(ExperimentalForeignApi::class)
private fun buildTabBar(
    items: List<NavigationItem>,
    colors: AdaptiveNavigationBarColors,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
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
                        UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate
                    )

                val selectedImage =
                    (
                        item.selectedSystemIcon?.let {
                            UIImage.systemImageNamed(it) ?: UIImage.imageNamed(it)
                        } ?: normalImage
                    )?.imageWithRenderingMode(
                        UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate
                    )

                UITabBarItem(
                    title = if (item.showLabel) item.title else null,
                    image = normalImage,
                    selectedImage = selectedImage
                ).apply {
                    tag = index.toLong()
                    // No fixed `itemWidth`: the bar uses Fill positioning (see applyTabBarColors) so items
                    // distribute evenly across the full bar width.
                    enabled = item.enabled
                    badgeValue = item.badge
                    badgeColor = colors.badgeContainerColor.toUIColor()
                    setBadgeTextAttributes(
                        mapOf(
                            NSForegroundColorAttributeName to colors.badgeContentColor.toUIColor()
                        ),
                        UIControlStateNormal
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
    colors: AdaptiveNavigationBarColors
) {
    val appearance =
        UITabBarAppearance().apply {
            configureWithTransparentBackground()

            backgroundEffect =
                UIBlurEffect.effectWithStyle(
                    UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial
                )

            backgroundColor =
                colors.containerColor
                    .toUIColor()
                    .colorWithAlphaComponent(0.72)

            shadowColor = UIColor.clearColor

            stackedLayoutAppearance.selected.titleTextAttributes =
                mapOf(
                    NSForegroundColorAttributeName to colors.selectedTextColor.toUIColor()
                )
            stackedLayoutAppearance.selected.iconColor = colors.selectedIconColor.toUIColor()
            stackedLayoutAppearance.normal.iconColor = colors.unselectedIconColor.toUIColor()
            stackedLayoutAppearance.selected.badgeBackgroundColor = colors.badgeContainerColor.toUIColor()
        }
    tabBar.standardAppearance = appearance
    tabBar.scrollEdgeAppearance = appearance
    // `itemPositioning` is a UITabBar property (not an appearance one). Fill (not the upstream Centered)
    // distributes items evenly across the full bar width, so the stacked icon/label never overflows.
    tabBar.itemPositioning = UITabBarItemPositioning.UITabBarItemPositioningFill
}

internal fun Color.toUIColor(): UIColor =
    UIColor(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble()
    )

private class TabBarDelegate(
    val onItemSelected: (Int) -> Unit
) : NSObject(),
    UITabBarDelegateProtocol {
    override fun tabBar(
        tabBar: UITabBar,
        didSelectItem: UITabBarItem
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
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        windowInsets = windowInsets,
        containerColor = colors.containerColor
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
                        indicatorColor = colors.indicatorColor
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
                                                y = (-2).dp
                                            ),
                                        containerColor = colors.badgeContainerColor,
                                        contentColor = colors.badgeContentColor
                                    ) {
                                        Text(item.badge)
                                    }
                                }

                                item.showBadgeDot -> {
                                    Badge(
                                        modifier =
                                            Modifier.offset(
                                                x = 4.dp,
                                                y = (-2).dp
                                            ),
                                        containerColor = colors.badgeContainerColor
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    when {
                                        selected && item.selectedIcon != null ->
                                            item.selectedIcon

                                        else ->
                                            item.icon
                                    }
                                ),
                            contentDescription =
                                item.contentDescription ?: item.title
                        )
                    }
                },
                label = {
                    if (item.showLabel) {
                        Text(item.title)
                    }
                },
                alwaysShowLabel = item.showLabel
            )
        }
    }
}