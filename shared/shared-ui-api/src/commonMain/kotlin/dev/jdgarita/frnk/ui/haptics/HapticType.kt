package dev.jdgarita.frnk.ui.haptics

/**
 * Semantic haptic feedback vocabulary the toolkit exposes to host apps and atoms. Each entry
 * describes *intent* ("a selection changed", "an operation succeeded") rather than a concrete
 * waveform, so callers never reach for the underlying haptics library. The active [HapticEngine]
 * maps each type to a platform effect (see the `MultiHapticEngine` binding in `:shared-ui-atoms`).
 */
enum class HapticType {
    /** A sharp, crisp tap — the default for button/icon-button presses. */
    Click,

    /** A light tick — toggles, segmented controls, tab switches, list selections. */
    Selection,

    /** A heavier, weightier press — long-press / press-and-hold affordances. */
    LongPress,

    /** A positive confirmation — a completed/successful operation. */
    Success,

    /** A cautionary cue — a recoverable problem or a destructive confirmation. */
    Warning,

    /** A negative cue — a failed operation or invalid input. */
    Error,
}
