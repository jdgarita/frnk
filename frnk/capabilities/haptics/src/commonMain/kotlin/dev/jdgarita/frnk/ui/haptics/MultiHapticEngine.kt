package dev.jdgarita.frnk.ui.haptics

import top.ltfan.multihaptic.DelayType
import top.ltfan.multihaptic.vibrator.Vibrator
import kotlin.time.Duration.Companion.milliseconds

/**
 * Maps the toolkit's semantic [HapticType] onto `multihaptic` primitive compositions and plays them
 * through a [Vibrator]. This is the one place that knows about the haptics library; everything else
 * speaks [HapticType]. No-ops when the device/platform reports no vibration support (e.g. an iOS
 * simulator), so callers never have to guard.
 *
 * The [vibrator] is resolved *per call* via a provider rather than captured once, so this long-lived
 * engine always plays through the currently-valid [Vibrator]. That matters on iOS: the underlying
 * `CHHapticEngine` is stopped when the app is backgrounded and `rememberFrnkHaptics` rebuilds the
 * vibrator on return to foreground — the provider lets those rebuilds reach this engine without
 * re-creating the surrounding [DefaultHapticFeedback] (which would reset the enabled flag).
 *
 * The compositions below are intentionally simple and tunable — single primitives for the common
 * cues and short two-primitive sequences for the notification-style ones.
 */
class MultiHapticEngine(
    private val vibrator: () -> Vibrator
) : HapticEngine {
    /** Convenience for a fixed vibrator (hosts that don't need foreground-driven rebuilds). */
    constructor(vibrator: Vibrator) : this({ vibrator })

    override fun emit(type: HapticType) {
        val current = vibrator()
        if (!current.isVibrationSupported) return
        when (type) {
            HapticType.Click -> current.vibrate { click() }
            HapticType.Selection -> current.vibrate { tick() }
            HapticType.LongPress -> current.vibrate { thud() }
            HapticType.Success ->
                current.vibrate {
                    click()
                    quickRise { delay = 60.milliseconds }
                }
            HapticType.Warning ->
                current.vibrate {
                    thud()
                    tick { delay = 80.milliseconds }
                }
            HapticType.Error ->
                current.vibrate {
                    thud()
                    thud {
                        delay = 90.milliseconds
                        delayType = DelayType.Pause
                    }
                }
        }
    }
}