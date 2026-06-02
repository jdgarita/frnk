package dev.jdgarita.frnk.ui.haptics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Records every [HapticType] handed to [emit], so tests can assert what (if anything) fired. */
private class RecordingHapticEngine : HapticEngine {
    val emitted = mutableListOf<HapticType>()

    override fun emit(type: HapticType) {
        emitted += type
    }
}

class DefaultHapticFeedbackTest {
    @Test
    fun enabled_perform_emitsMappedType() {
        val engine = RecordingHapticEngine()
        val haptics = DefaultHapticFeedback(engine, initiallyEnabled = true)

        haptics.perform(HapticType.Success)

        assertEquals(listOf(HapticType.Success), engine.emitted)
    }

    @Test
    fun disabled_perform_isNoOp() {
        val engine = RecordingHapticEngine()
        val haptics = DefaultHapticFeedback(engine, initiallyEnabled = false)

        haptics.perform(HapticType.Click)

        assertTrue(engine.emitted.isEmpty(), "perform must not emit while disabled")
    }

    @Test
    fun setEnabled_gatesAndExposesFlag() {
        val engine = RecordingHapticEngine()
        val haptics = DefaultHapticFeedback(engine, initiallyEnabled = true)
        assertTrue(haptics.isEnabled.value)

        haptics.setEnabled(false)
        assertFalse(haptics.isEnabled.value)
        haptics.perform(HapticType.Click)
        assertTrue(engine.emitted.isEmpty(), "no emit after disabling")

        haptics.setEnabled(true)
        assertTrue(haptics.isEnabled.value)
        haptics.perform(HapticType.Error)
        assertEquals(listOf(HapticType.Error), engine.emitted)
    }
}
