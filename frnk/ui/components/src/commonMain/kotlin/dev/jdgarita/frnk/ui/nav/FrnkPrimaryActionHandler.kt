package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The [FrnkPrimaryActionRegistry] of the nearest primary-action-capable scaffold, or `null` when the
 * screen isn't hosted under one. Declared here (where the screens that read it live, next to
 * [FrnkPrimaryActionHandler]); `:shared-ui-nav`'s `FrnkTabbedNavScaffold` provides the real instance —
 * the same declared-in-atoms / provided-in-nav split as `LocalFrnkBottomBarInset`.
 *
 * Static (`staticCompositionLocalOf`, unlike the inset local) because the registry **instance** never
 * changes for the lifetime of the provider — per-tap state flows through the registry's `StateFlow`,
 * not through the local.
 */
val LocalFrnkPrimaryActionRegistry = staticCompositionLocalOf<FrnkPrimaryActionRegistry?> { null }

/**
 * While composed (and [enabled]), the current screen owns the bottom bar's **primary-action button**:
 * the button shows and taps invoke [onAction]. On dispose (the screen leaves the back stack) or when
 * [enabled] flips off, the claim is released and the button falls back to the previously registered
 * handler, the host-level default, or hidden.
 *
 * Canonical MVI usage — route the tap through the screen's ViewModel as an intent, never do work in
 * the lambda itself:
 *
 * ```kotlin
 * FrnkPrimaryActionHandler { onIntent(HomeIntent.PrimaryActionClicked) }
 * ```
 *
 * No-op when no scaffold provides [LocalFrnkPrimaryActionRegistry] (e.g. the screen is composed
 * outside `FrnkTabbedNavScaffold`, or the host didn't pass a registry). [onAction] is observed via
 * [rememberUpdatedState], so a recomposed handler is picked up without re-registering.
 */
@Composable
fun FrnkPrimaryActionHandler(
    enabled: Boolean = true,
    onAction: () -> Unit,
) {
    val registry = LocalFrnkPrimaryActionRegistry.current ?: return
    val currentOnAction by rememberUpdatedState(onAction)
    DisposableEffect(registry, enabled) {
        if (!enabled) {
            onDispose {}
        } else {
            val registration = registry.register { currentOnAction() }
            onDispose { registration.unregister() }
        }
    }
}
