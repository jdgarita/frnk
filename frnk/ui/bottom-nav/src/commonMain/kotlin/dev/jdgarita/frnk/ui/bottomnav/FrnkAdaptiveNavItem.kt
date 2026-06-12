package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource

/**
 * A single item rendered by the [FrnkAdaptiveNavBarBottomBar]. The `adaptive-nav-bar` library takes
 * **resource-based** icons: a [androidIcon] `DrawableResource` for the Android Material3 bar and an
 * [iosSystemIcon] SF-Symbol name (or Xcode asset name) for the iOS bar. Optional `selected*` variants swap
 * the icon when the item is active; when null the base icon is reused.
 */
@Immutable
data class FrnkAdaptiveNavItem(
    val key: String,
    val label: String,
    val androidIcon: DrawableResource,
    val iosSystemIcon: String,
    val selectedAndroidIcon: DrawableResource? = null,
    val selectedIosSystemIcon: String? = null,
)
