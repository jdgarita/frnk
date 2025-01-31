package dev.jdgarita.frnk.domain.framework

sealed class AnalyticsEvent(
    val eventName: String,
    val attributes: Map<String, String>,
    val eventType: AnalyticsEventType = AnalyticsEventType.Telemetry
)