package dev.jdgarita.frnk.demo

import androidx.compose.ui.window.ComposeUIViewController
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import platform.UIKit.UIViewController

/**
 * Swift entry point. iosDemoApp wraps this in a `UIViewControllerRepresentable` and routes
 * MVI effects back through the [onEffect] lambda.
 */
fun MainViewController(onEffect: (DemoEffect) -> Unit = {}): UIViewController =
    ComposeUIViewController {
        FrnkTheme(config = demoBlueThemeConfig()) {
            DemoScreen(onEffect = onEffect)
        }
    }
