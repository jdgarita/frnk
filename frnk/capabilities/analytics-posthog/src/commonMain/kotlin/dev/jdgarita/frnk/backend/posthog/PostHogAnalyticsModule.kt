package dev.jdgarita.frnk.backend.posthog

import com.posthog.kmp.PersonProfiles
import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogConfig
import com.posthog.kmp.PostHogContext
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.utils.PrintLogger
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

/**
 * PostHog as the toolkit's one `AnalyticsTracker`. Hosts don't install this themselves:
 * `frnkModules { observability(postHog = …, sentry = …) }` (`:ui-app`) does, from the config the host
 * passes. It stays public for the raw `initializeFrnk(modules = listOf(…))` path and for tests.
 *
 * The binding is `createdAtStart`, so `PostHog.setup` runs inside `startKoin` on every bootstrap
 * path and the SDK's own lifecycle events see the launch. The SDK is set up exactly once per
 * process; a second Koin start (tests, a host that restarts its graph) does not re-run it. A missing
 * key never reaches here — [PostHogAnalyticsConfig] rejects it at construction.
 */
fun postHogAnalyticsModule(config: PostHogAnalyticsConfig): Module =
    module {
        single<AnalyticsTracker>(createdAtStart = true) {
            // The context lookup runs inside the runCatching too: a graph started without Koin's
            // androidContext (a bare startKoin) degrades to a logged no-op rather than failing start.
            setUpPostHogOnce(config) { platformPostHogContext() }
            PostHogAnalyticsTracker()
        }
    }

/** The platform half of `PostHog.setup`: Android needs the `Application` (from Koin's `androidContext`), iOS nothing. */
internal expect fun Scope.platformPostHogContext(): PostHogContext

private var started = false

private fun setUpPostHogOnce(
    config: PostHogAnalyticsConfig,
    context: () -> PostHogContext
) {
    if (started) return
    runCatching {
        val platformContext = context()
        PostHog.setup(
            config =
                PostHogConfig(
                    apiKey = config.apiKey,
                    host = config.host,
                    debug = config.debug,
                    optOut = config.optOut,
                    captureApplicationLifecycleEvents = config.captureApplicationLifecycleEvents,
                    // Screens come through AnalyticsTracker.screen; the SDK's Activity-based capture
                    // sees one Activity in a Compose app and would report nothing useful.
                    captureScreenViews = false,
                    // Anonymous events create no person until identify(); the toolkit identifies
                    // with the app's anonymous id at bootstrap, which merges them in.
                    personProfiles = PersonProfiles.IDENTIFIED_ONLY
                ),
            context = platformContext
        )
        PostHog.register(ENVIRONMENT_PROPERTY, config.environment)
        started = true
    }.onFailure { PrintLogger.w(TAG, "PostHog.setup failed, analytics is a no-op: ${it.message}") }
}

private const val TAG = "PostHogAnalytics"
private const val ENVIRONMENT_PROPERTY = "environment"