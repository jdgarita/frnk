package dev.jdgarita.frnk.ui.haptics

import kotlinx.coroutines.flow.StateFlow

/**
 * The toolkit's **simplified, host-facing haptics API**. A single instance is installed by
 * `FrnkTheme` as the `LocalFrnkHaptics` composition local (in `:shared-ui-atoms`), so any composable
 * — toolkit atom or host screen — fires feedback with one line:
 *
 * ```
 * val haptics = LocalFrnkHaptics.current
 * FrnkButton(onClick = { haptics.perform(HapticType.Success); save() }, ...)
 * ```
 *
 * Frnk's interactive atoms already call [perform] on press, so "vibrate on interactions" works with
 * zero host code; the [isEnabled] flag (driven by the Settings "Haptic feedback" toggle) gates every
 * call. Compose-free on purpose — feature ViewModels can inject it too.
 */
interface HapticFeedback {
    /** Whether haptics are currently enabled. [perform] is a no-op while this is `false`. */
    val isEnabled: StateFlow<Boolean>

    /** Enable or disable haptics globally (the Settings toggle calls this). */
    fun setEnabled(enabled: Boolean)

    /** Emit the haptic for [type], unless disabled or unsupported by the device. */
    fun perform(type: HapticType)
}

/**
 * Low-level SPI the platform binding supplies to [DefaultHapticFeedback]: turn a semantic
 * [HapticType] into an actual device vibration. The `multihaptic` binding (`MultiHapticEngine` in
 * `:shared-ui-atoms`) implements this; tests use a fake. Kept separate from [HapticFeedback] so the
 * enabled-flag/gating logic stays Compose- and library-free here.
 */
fun interface HapticEngine {
    fun emit(type: HapticType)
}