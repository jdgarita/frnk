package dev.jdgarita.frnk.utils

import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

actual fun applyNativeInterfaceStyle(dark: Boolean?) {
    val style =
        when (dark) {
            true -> UIUserInterfaceStyleDark
            false -> UIUserInterfaceStyleLight
            null -> UIUserInterfaceStyleUnspecified
        }
    // Pin every window of every connected scene. Setting it on the window cascades to the whole view
    // hierarchy (and to native views created later), so a freshly built UIKitView inherits the forced
    // style immediately instead of resolving the system trait first. `Unspecified` (System appearance)
    // hands control back to the OS.
    UIApplication.sharedApplication.connectedScenes.forEach { scene ->
        (scene as? UIWindowScene)?.windows?.forEach { window ->
            (window as? UIWindow)?.setOverrideUserInterfaceStyle(style)
        }
    }
}
