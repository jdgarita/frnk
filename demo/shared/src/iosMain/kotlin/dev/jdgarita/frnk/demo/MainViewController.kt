package dev.jdgarita.frnk.demo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Swift entry point. iosDemoApp wraps this in a `UIViewControllerRepresentable`. [FrnkDemoApp] is the
 * unified shared entry point — the same composable demo-android's `MainActivity` calls — and owns the
 * theme wrap (via `:ui-app`'s `FrnkApp`), so there's nothing left to set up here.
 *
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        FrnkDemoApp()
    }