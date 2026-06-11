package dev.jdgarita.frnk.demo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Swift entry point. iosDemoApp wraps this in a `UIViewControllerRepresentable` and routes
 * MVI effects back through the [onEffect] lambda. `DemoScreen` owns the theme wrap now (via
 * `FrnkAppShell`), so there's nothing left to set up here.
 */
fun MainViewController(onEffect: (DemoEffect) -> Unit = {}): UIViewController =
    ComposeUIViewController {
        DemoScreen(onEffect = onEffect)
    }
