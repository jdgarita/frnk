package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingGate

/**
 * Presents the built-in onboarding flow **once, on first launch**: when [enabled] and onboarding has
 * not been seen, navigates to [FrnkRoute.Onboarding] (pushed onto the current tab — typically Home,
 * so closing it pops back to Home) and records it as seen so later launches don't re-present it.
 *
 * Drop it into the [FrnkTabbedNavScaffold] `effects` slot, where the [FrnkAppScope] is live and a single
 * collector survives tab swaps. `FrnkAppScaffold` (`:ui-app`) wires this automatically from a
 * `rememberOnboardingGate()`; hosts that bypass `:ui-app` (e.g. the demo, which composes
 * [FrnkTabbedNavScaffold] directly) call it themselves.
 *
 * **Precondition:** [enabled] must imply that [FrnkRoute.Onboarding] is registered as a nav entry
 * (in [FrnkTabbedNavScaffold] that means `onboardingPages` is non-empty). Navigating to an unregistered route
 * throws from nav3's `NavDisplay`, so callers must derive [enabled] from the same condition that
 * registers the destination — `FrnkAppScaffold` uses `showOnboardingOnFirstLaunch && onboardingPages.isNotEmpty()`.
 *
 * **Persistence:** when [gate] is non-null the seen-flag is persisted (survives process death). When
 * it is `null` (no `KeyValueStore` bound) the effect falls back to a saved-state **session** flag, so
 * onboarding still shows — once per app run instead of never. The navigation is issued **before** the
 * flag is recorded, so a registration/seen check that passes is always followed by an attempt to show.
 *
 * @param scope the shell's app scope (drives the back stack).
 * @param gate the persisted first-launch gate, or `null` to fall back to a session-only flag.
 * @param enabled gate the whole behavior (e.g. only when onboarding pages are supplied).
 */
@Composable
fun FrnkFirstLaunchOnboardingEffect(
    scope: FrnkAppScope,
    gate: OnboardingGate?,
    enabled: Boolean
) {
    if (!enabled) return
    // Session fallback for hosts with no persistent store: survives config changes via saved state,
    // resets on a true cold start — so onboarding shows once per app run rather than never.
    var shownThisSession by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val alreadySeen = gate?.seen ?: shownThisSession
        if (!alreadySeen) {
            scope.navigateTo(FrnkRoute.Onboarding)
            gate?.markSeen()
            shownThisSession = true
        }
    }
}