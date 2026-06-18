package dev.jdgarita.frnk.ui.haptics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A [HapticFeedback] that does nothing — the `LocalFrnkHaptics` default for composables rendered
 * outside a `FrnkTheme` (e.g. previews, tests), so atoms can call [perform] unconditionally without
 * a null check or a crash. Reports [isEnabled] as `false`.
 */
object NoOpHapticFeedback : HapticFeedback {
    override val isEnabled: StateFlow<Boolean> = MutableStateFlow(false)

    override fun setEnabled(enabled: Boolean) = Unit

    override fun perform(type: HapticType) = Unit
}