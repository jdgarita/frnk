package dev.jdgarita.frnk.utils

/**
 * Reflects the toolkit's chosen appearance onto the host platform's **native** interface style, so
 * native chrome that is *not* Compose-rendered follows the toolkit theme instead of the device's
 * system setting.
 *
 * [dark] = `true` forces dark, `false` forces light, `null` defers to the system.
 *
 * On **iOS** this sets `overrideUserInterfaceStyle` on the app's windows. Without it, native `UIKit`
 * views (e.g. the adaptive bottom bar's `UIBlurEffect` / `UIGlassEffect` materials) resolve their
 * appearance against the *system* trait collection — so when the app is forced to the opposite theme
 * (e.g. light on a dark-mode device) a freshly created/recreated native view flashes the system style
 * for a frame before settling. Pinning the window's interface style removes that mismatch. On
 * **Android** the toolkit's light/dark is fully Compose-driven, so this is a no-op.
 *
 * Like [PlatformInfo], this is a narrow `expect/actual` that crosses only plain data — it never leaks a
 * `UIWindow` / `Context` up the graph. Must be called on the **main thread**; the toolkit calls it from
 * a Compose effect inside `FrnkTheme`.
 */
expect fun applyNativeInterfaceStyle(dark: Boolean?)
