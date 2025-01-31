package dev.jdgarita.frnk.domain.framework

/**
 * Collection of possible event names that can be sent
 */
enum class AnalyticsEventType(val stringValue: String) {
    Advertisement("ClientAdEvent"),
    Telemetry("ClientAnalytic")
}