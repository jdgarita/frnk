package dev.jdgarita.frnk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/** Settings › Accessibility › Motion › Reduce Motion. */
@Composable
internal actual fun platformReducedMotion(): Boolean = remember { UIAccessibilityIsReduceMotionEnabled() }