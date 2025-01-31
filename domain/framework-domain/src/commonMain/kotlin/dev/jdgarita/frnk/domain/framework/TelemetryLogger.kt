package dev.jdgarita.frnk.domain.framework

interface TelemetryLogger {
    fun setUserId(userId: String)

    fun setUserProperty(key: String, value: String, clearOnReset: Boolean)

    fun reset()

    fun breadcrumb(breadcrumb: String)

    fun log(
        severity: Severity,
        message: String,
        properties: Map<String, String> = emptyMap(),
        throwable: Throwable? = null
    )

    fun startSpan(event: String, identifier: String, properties: Map<String, String> = emptyMap())

    fun endSpan(event: String, identifier: String, properties: Map<String, String> = emptyMap())

    fun logEvent(event: AnalyticsEvent)

    enum class Severity {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }
}