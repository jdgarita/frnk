package dev.jdgarita.frnk.backend.posthog

import com.posthog.kmp.PostHogConfig

/**
 * Everything a host tells frnk about its PostHog project.
 *
 * @property apiKey The project API key (`phc_…`), a public client key. Blank means "not configured":
 *   [postHogAnalyticsModule] then binds the no-op tracker and never touches the SDK.
 * @property host The ingestion host of the project's region ([PostHogConfig.HOST_US] / `HOST_EU`).
 * @property environment Registered as a super property on every event (`debug` / `release`), so one
 *   PostHog project serves both and an insight filters testers out.
 * @property debug SDK diagnostics to the console.
 * @property optOut Start opted out (nothing is sent until the host calls the SDK's `optIn`).
 * @property captureApplicationLifecycleEvents The SDK's `Application Installed / Updated / Opened /
 *   Backgrounded` events.
 */
data class PostHogAnalyticsConfig(
    val apiKey: String,
    val environment: String,
    val host: String = DEFAULT_HOST,
    val debug: Boolean = false,
    val optOut: Boolean = false,
    val captureApplicationLifecycleEvents: Boolean = true
) {
    val isConfigured: Boolean get() = apiKey.isNotBlank()

    companion object {
        /** PostHog Cloud US — the [host] a project created without picking a region lands on. */
        const val DEFAULT_HOST: String = PostHogConfig.HOST_US

        /** PostHog Cloud EU. */
        const val EU_HOST: String = PostHogConfig.HOST_EU
    }
}