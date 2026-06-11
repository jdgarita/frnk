package dev.jdgarita.frnk.utils

import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
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
    // Pin only the windows of **foreground-active** window scenes, not every connected scene — so a
    // secondary surface (CarPlay, an external/AirPlay display, another iPad window) the toolkit doesn't
    // own isn't forced into the app's theme. Setting it on a window cascades to its whole view hierarchy
    // (and to native views created later within it), so a freshly built UIKitView inherits the forced
    // style immediately instead of resolving the system trait first. `Unspecified` (System appearance)
    // hands control back to the OS. Scope note: this is the toolkit's zero-config bridge for an app that
    // lets FrnkTheme drive its appearance; a host that manages its own window interface style should keep
    // FrnkTheme on `Appearance.System` (→ Unspecified, a no-op pin).
    UIApplication.sharedApplication.connectedScenes
        .mapNotNull { it as? UIWindowScene }
        .filter { it.activationState == UISceneActivationStateForegroundActive }
        .forEach { scene ->
            scene.windows.forEach { window ->
                (window as? UIWindow)?.setOverrideUserInterfaceStyle(style)
            }
        }
}
