package dev.jdgarita.frnk.ui.haptics

import kotlinx.coroutines.flow.StateFlow

/**
 * A [HapticFeedback] whose enabled flag *is* a [HapticsPreference]: [isEnabled] reads it,
 * [setEnabled] writes it, and [perform] is a no-op while it is off. Vibration is delegated to
 * [engine], as in [DefaultHapticFeedback].
 *
 * This is what makes the Settings toggle durable with no extra wiring: `rememberFrnkSettingsHandler`
 * already calls `LocalFrnkHaptics.current.setEnabled(checked)`, and when the ambient instance is this
 * class that call lands in the preference. `FrnkApp` installs it whenever a [HapticsPreference] is
 * bound; [rememberFrnkHaptics] builds it for hosts that compose `FrnkTheme` themselves.
 */
class PersistentHapticFeedback(
    private val engine: HapticEngine,
    private val preference: HapticsPreference
) : HapticFeedback {
    override val isEnabled: StateFlow<Boolean> get() = preference.isEnabled

    override fun setEnabled(enabled: Boolean) = preference.setEnabled(enabled)

    override fun perform(type: HapticType) {
        if (preference.isEnabled.value) engine.emit(type)
    }
}