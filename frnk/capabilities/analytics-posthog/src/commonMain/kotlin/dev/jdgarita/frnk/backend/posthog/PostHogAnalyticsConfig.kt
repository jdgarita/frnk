package dev.jdgarita.frnk.backend.posthog

import com.posthog.kmp.PostHogConfig

/**
 * How frnk reports to PostHog. Hosts normally construct nothing here — `frnkModules {
 * observability(sentry = …) }` derives the PostHog config from the Sentry one — and only pass an
 * explicit `PostHogAnalyticsConfig(environment = …, debug = …)` to tune a flag.
 *
 * @property environment Registered as a super property on every event (`debug` / `release`), so one
 *   PostHog project serves both and an insight filters testers out.
 * @property apiKey The project API key (`phc_…`), a public client key. Defaults to the toolkit-wide
 *   [FrnkPostHogProject.API_KEY]: every frnk app reports to the same PostHog project, so no host
 *   supplies a key. A blank key is a configuration error and fails here, at construction, rather
 *   than degrading to a tracker that silently drops every event — there is no no-op to fall back to.
 * @property host The ingestion host of the project's region ([PostHogConfig.HOST_US] / `HOST_EU`).
 * @property debug SDK diagnostics to the console.
 * @property optOut Start opted out (nothing is sent until the host calls the SDK's `optIn`).
 * @property captureApplicationLifecycleEvents The SDK's `Application Installed / Updated / Opened /
 *   Backgrounded` events.
 */
data class PostHogAnalyticsConfig(
    val environment: String,
    val apiKey: String = FrnkPostHogProject.API_KEY,
    val host: String = DEFAULT_HOST,
    val debug: Boolean = false,
    val optOut: Boolean = false,
    val captureApplicationLifecycleEvents: Boolean = true
) {
    init {
        require(apiKey.isNotBlank()) {
            "PostHogAnalyticsConfig.apiKey is blank — frnk hosts always ship PostHog analytics; " +
                "leave apiKey at its default (the toolkit-wide FrnkPostHogProject.API_KEY)"
        }
    }

    companion object {
        /** The shared frnk project's region ([FrnkPostHogProject.HOST], PostHog Cloud US). */
        const val DEFAULT_HOST: String = FrnkPostHogProject.HOST

        /** PostHog Cloud EU. */
        const val EU_HOST: String = PostHogConfig.HOST_EU
    }
}