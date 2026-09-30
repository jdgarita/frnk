package dev.jdgarita.frnk.ui.haptics

import kotlinx.coroutines.flow.StateFlow

/**
 * The durable on/off switch for haptics — the value the Settings "Haptic feedback" toggle stores.
 *
 * [HapticFeedback] already carries an enabled flag, but [DefaultHapticFeedback] keeps it in memory
 * only, so a user who turned haptics off feels them again after the next launch. A host that wants
 * the switch to persist binds a [HapticsPreference] (usually [KeyValueHapticsPreference] via
 * [hapticsPreferenceModule]); `FrnkApp` then installs a [PersistentHapticFeedback] over it, so the
 * ambient `LocalFrnkHaptics` reads and writes this preference directly — one source of truth, no
 * sync effect.
 *
 * Why it matters: `multihaptic` drives the vibrator directly and never consults the OS
 * touch-feedback setting, so the in-app switch is the only way a user can turn haptics off.
 *
 * Compose-free, so ViewModels can inject it.
 */
interface HapticsPreference {
    /** The stored value. Hot: every [setEnabled] is reflected here immediately. */
    val isEnabled: StateFlow<Boolean>

    /** Persists [enabled] and publishes it on [isEnabled]. */
    fun setEnabled(enabled: Boolean)
}