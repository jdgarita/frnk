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
 * The compositions below are intentionally simple and tunable — single primitives for the common
 * cues and short two-primitive sequences for the notification-style ones.
 */
class MultiHapticEngine(
    private val vibrator: Vibrator,
) : HapticEngine {
    override fun emit(type: HapticType) {
        if (!vibrator.isVibrationSupported) return
        when (type) {
            HapticType.Click -> vibrator.vibrate { click() }
            HapticType.Selection -> vibrator.vibrate { tick() }
            HapticType.LongPress -> vibrator.vibrate { thud() }
            HapticType.Success ->
                vibrator.vibrate {
                    click()
                    quickRise { delay = 60.milliseconds }
                }
            HapticType.Warning ->
                vibrator.vibrate {
                    thud()
                    tick { delay = 80.milliseconds }
                }
            HapticType.Error ->
                vibrator.vibrate {
                    thud()
                    thud {
                        delay = 90.milliseconds
                        delayType = DelayType.Pause
                    }
                }
        }
    }
}
