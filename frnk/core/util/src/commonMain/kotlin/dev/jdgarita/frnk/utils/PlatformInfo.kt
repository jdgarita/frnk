package dev.jdgarita.frnk.utils

/**
 * Read-only description of the running platform, for diagnostics such as a prefilled feedback
 * e-mail. Implemented per target with each platform's own SDK — no `Context` or composition is
 * required, so it is safe to read from anywhere in common code.
 */
expect object PlatformInfo {
    /** Human-readable OS name, e.g. "Android" or "iOS". */
    val osName: String

    /** OS version string, e.g. "14" or "17.4". */
    val osVersion: String

    /** Best-effort device model, e.g. "Google Pixel 8" or "iPhone". */
    val deviceModel: String
}