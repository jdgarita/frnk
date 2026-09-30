package dev.jdgarita.frnk.ui.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import top.ltfan.multihaptic.compose.rememberVibrator

/** Stable id for the toolkit's "Haptic feedback" Settings toggle (mirrors `GOD_MODE_TOGGLE_ID`). */
const val HAPTICS_TOGGLE_ID = "haptics"

/**
 * The ambient [HapticFeedback] for everything under a `FrnkTheme`. Toolkit atoms and host composables
 * read `LocalFrnkHaptics.current` and call [HapticFeedback.perform]. Defaults to [NoOpHapticFeedback]
 * so composables rendered outside `FrnkTheme` (previews, tests) are safe; `FrnkTheme` overrides it
 * with a real [rememberFrnkHaptics] instance.
 *
 * `static` because the installed instance is stable for a `FrnkTheme`'s lifetime — the underlying
 * `Vibrator` may be rebuilt at runtime (see [rememberFrnkHaptics]) but the wrapping [HapticFeedback]
 * is not — so the many atoms that read `.current` don't each register as change-observers, mirroring
 * `LocalIndication`.
 */
val LocalFrnkHaptics: ProvidableCompositionLocal<HapticFeedback> =
    staticCompositionLocalOf { NoOpHapticFeedback }

/**
 * Builds the `multihaptic`-backed [HapticFeedback] `FrnkTheme` installs. Resolves the platform
 * [top.ltfan.multihaptic.vibrator.Vibrator] via `multihaptic-compose`'s `rememberVibrator()` (reads
 * `LocalContext` on Android, Core Haptics on iOS — no Context plumbing), then wraps it in
 * [DefaultHapticFeedback] (enabled by default).
 *
 * **iOS lifecycle fix:** multihaptic's `CHHapticEngine` is stopped by the system when the app is
 * backgrounded and is never restarted on resume (0.3.2 wires only a `resetHandler`, not a
 * `stoppedHandler`), so haptics silently die after the user leaves and returns to the app — e.g. via
 * the "Manage Subscription" → App Store hop — until the process is killed. To recover, the vibrator
 * (and thus a fresh, started engine) is rebuilt on every return to foreground, keyed off
 * [rememberForegroundCount]. The [DefaultHapticFeedback] wrapper itself stays stable across rebuilds
 * so the enabled flag (the Settings toggle) survives, and the engine reads the latest vibrator through
 * a provider. On Android the rebuild is a cheap no-op-equivalent (the system `Vibrator` is stateless).
 *
 * Runtime enable/disable goes through [HapticFeedback.setEnabled] (the Settings toggle) on the
 * returned instance; the flag lives in memory only. For a switch that survives relaunch, bind a
 * [HapticsPreference] ([hapticsPreferenceModule]) — `FrnkApp` then uses the `rememberFrnkHaptics(preference)`
 * overload — or pass any other [HapticFeedback] to `FrnkTheme(haptics = …)`.
 */
@Composable
fun rememberFrnkHaptics(): HapticFeedback {
    val engine = rememberFrnkHapticEngine()
    return remember { DefaultHapticFeedback(engine) }
}

/**
 * The same `multihaptic`-backed haptics as [rememberFrnkHaptics], with the enabled flag stored in
 * [preference] instead of in memory: returns a [PersistentHapticFeedback], so `isEnabled` starts at the
 * stored value and every `setEnabled` (the Settings toggle) is written back. The foreground rebuild of
 * the vibrator described on [rememberFrnkHaptics] applies here too.
 *
 * `FrnkApp` calls this for you when a [HapticsPreference] is bound (see [hapticsPreferenceModule]);
 * call it yourself only when you compose `FrnkTheme(haptics = …)` directly.
 */
@Composable
fun rememberFrnkHaptics(preference: HapticsPreference): HapticFeedback {
    val engine = rememberFrnkHapticEngine()
    return remember(preference) { PersistentHapticFeedback(engine, preference) }
}

/**
 * The stable [HapticEngine] both [rememberFrnkHaptics] overloads wrap: it reads the current vibrator
 * through a provider, and the vibrator is rebuilt on every return to foreground (see
 * [rememberFrnkHaptics] for why).
 */
@Composable
private fun rememberFrnkHapticEngine(): HapticEngine {
    val foregroundCount = rememberForegroundCount()
    val vibrator = key(foregroundCount) { rememberVibrator() }
    val currentVibrator = rememberUpdatedState(vibrator)
    return remember { MultiHapticEngine { currentVibrator.value } }
}

/**
 * Counts returns to the foreground (each `ON_STOP` → `ON_START` transition); the initial start that
 * the lifecycle replays on observer registration is ignored. Driving [rememberFrnkHaptics]'s
 * `key(...)` off this rebuilds the platform vibrator after the app was backgrounded, which on iOS
 * restarts the otherwise-dead `CHHapticEngine`.
 */
@Composable
private fun rememberForegroundCount(): Int {
    val lifecycleOwner = LocalLifecycleOwner.current
    var count by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        var wasStopped = false
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> wasStopped = true
                    Lifecycle.Event.ON_START ->
                        if (wasStopped) {
                            wasStopped = false
                            count++
                        }
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return count
}