package dev.jdgarita.frnk.ui.haptics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The frnk-owned [HapticFeedback] implementation: holds the enabled flag and gates every [perform]
 * call, delegating actual vibration to a pluggable [HapticEngine]. Library- and Compose-free, so it
 * lives in `:shared-ui-api`; the `multihaptic`-backed engine is supplied by `:shared-ui-atoms`
 * (`rememberFrnkHaptics`), which needs a Compose `Vibrator`.
 *
 * The [isEnabled] flag is in-memory by default — same trade-off as `AppearanceController`. Hosts that
 * want it to survive process death can hoist their own state into `FrnkTheme(haptics = ...)`.
 */
class DefaultHapticFeedback(
    private val engine: HapticEngine,
    initiallyEnabled: Boolean = true
) : HapticFeedback {
    private val _isEnabled = MutableStateFlow(initiallyEnabled)
    override val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    override fun perform(type: HapticType) {
        if (_isEnabled.value) engine.emit(type)
    }
}