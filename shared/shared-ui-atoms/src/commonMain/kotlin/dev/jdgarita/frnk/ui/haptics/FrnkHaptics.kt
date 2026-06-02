package dev.jdgarita.frnk.ui.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import top.ltfan.multihaptic.compose.rememberVibrator

/** Stable id for the toolkit's "Haptic feedback" Settings toggle (mirrors `GOD_MODE_TOGGLE_ID`). */
const val HAPTICS_TOGGLE_ID = "haptics"

/**
 * The ambient [HapticFeedback] for everything under a `FrnkTheme`. Toolkit atoms and host composables
 * read `LocalFrnkHaptics.current` and call [HapticFeedback.perform]. Defaults to [NoOpHapticFeedback]
 * so composables rendered outside `FrnkTheme` (previews, tests) are safe; `FrnkTheme` overrides it
 * with a real [rememberFrnkHaptics] instance.
 *
 * `static` because the installed instance is constant for a `FrnkTheme`'s lifetime (it only changes
 * if the underlying `Vibrator` identity changes, which never happens at runtime) — so the many atoms
 * that read `.current` don't each register as change-observers, mirroring `LocalIndication`.
 */
val LocalFrnkHaptics: ProvidableCompositionLocal<HapticFeedback> =
    staticCompositionLocalOf { NoOpHapticFeedback }

/**
 * Builds the `multihaptic`-backed [HapticFeedback] `FrnkTheme` installs. Resolves the platform
 * [top.ltfan.multihaptic.vibrator.Vibrator] via `multihaptic-compose`'s `rememberVibrator()` (reads
 * `LocalContext` on Android, Core Haptics on iOS — no Context plumbing), then wraps it in
 * [DefaultHapticFeedback] (enabled by default).
 *
 * Runtime enable/disable goes through [HapticFeedback.setEnabled] (the Settings toggle) on the
 * returned instance. Hosts wanting a different initial value or process-death-durable state build
 * their own `DefaultHapticFeedback(MultiHapticEngine(rememberVibrator()), initiallyEnabled = …)` (or
 * any [HapticFeedback]) and pass it to `FrnkTheme(haptics = …)`.
 */
@Composable
fun rememberFrnkHaptics(): HapticFeedback {
    val vibrator = rememberVibrator()
    return remember(vibrator) { DefaultHapticFeedback(MultiHapticEngine(vibrator)) }
}
