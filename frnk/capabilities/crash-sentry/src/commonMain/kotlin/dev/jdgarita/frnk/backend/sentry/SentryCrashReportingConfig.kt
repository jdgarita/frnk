package dev.jdgarita.frnk.backend.sentry

import io.sentry.kotlin.multiplatform.SentryOptions

/**
 * Everything a host tells frnk about its Sentry project. The [dsn] is the project's public client
 * key (Sentry → project → Settings → Client Keys), not the auth token that uploads symbols.
 *
 * @property dsn **Required**: a blank DSN is a configuration error and fails here, at construction,
 *   rather than degrading to a reporter that silently drops every crash — every frnk host ships real
 *   crash reporting, so there is no no-op to fall back to.
 * @property environment Tags every event (`debug` / `release`), so one Sentry project serves both.
 * @property release Sentry's release id (`<bundleId>@<version>+<build>`); `null` lets the SDK derive
 *   one from the platform package, which is fine for a single app but not stable across platforms.
 * @property debug SDK diagnostics to the console.
 * @property sampleRate Error sampling, `null` = every event.
 * @property configure Escape hatch to touch any other [SentryOptions] field after frnk's defaults.
 */
data class SentryCrashReportingConfig(
    val dsn: String,
    val environment: String,
    val release: String? = null,
    val debug: Boolean = false,
    val sampleRate: Double? = null,
    val configure: (SentryOptions) -> Unit = {}
) {
    init {
        require(dsn.isNotBlank()) {
            "SentryCrashReportingConfig.dsn is blank — frnk hosts always ship Sentry crash reporting; " +
                "pass the project's public client key (Sentry → Settings → Client Keys) from your build config"
        }
    }
}