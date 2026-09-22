package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.backend.posthog.FrnkPostHogProject
import dev.jdgarita.frnk.backend.posthog.PostHogAnalyticsConfig
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.utils.AppResult
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [frnkModules] assembly: observability is mandatory and always the PostHog + Sentry pair, the
 * monetization trio is bundled from just the provider, and extras are carried through. The XOR guarantee for the slots is a compile-time property (a
 * single `var`) — not testable at runtime.
 */
class FrnkModulesBuilderTest {
    private val customProviderModule = module { single { "fake-entitlement-provider" } }
    private val hostModule = module { single { "host-binding" } }
    private val sentry = SentryCrashReportingConfig(dsn = "https://key@o1.ingest.sentry.io/1", environment = "test")

    @Test
    fun observability_is_mandatory_and_the_error_names_the_call() {
        val failure = assertFailsWith<IllegalStateException> { frnkModules { } }
        assertTrue(failure.message.orEmpty().contains("observability(sentry"), "names the missing call")
        assertTrue(failure.message.orEmpty().contains("PostHog"), "says which providers it installs")
    }

    @Test
    fun posthog_config_is_derived_from_the_sentry_one_on_the_toolkit_project() {
        // The host holds only a Sentry DSN: PostHog's key is the toolkit-wide project's, and its
        // environment mirrors Sentry's so one insight filters debug builds out of both.
        val derived = PostHogAnalyticsConfig(environment = sentry.environment)
        assertEquals(FrnkPostHogProject.API_KEY, derived.apiKey, "toolkit key by default")
        assertEquals("test", derived.environment)
        assertTrue(FrnkPostHogProject.API_KEY.startsWith("phc_"), "a real PostHog project key ships with the toolkit")
    }

    @Test
    fun host_extras_cannot_shadow_the_observability_pair() {
        // A host module that (wrongly) binds its own AnalyticsTracker under allowOverride(true):
        // the toolkit pair is installed after the extras, so the real trackers still win.
        val rogue = module { single<AnalyticsTracker> { RogueTracker() } }
        val app =
            koinApplication {
                allowOverride(true)
                modules(
                    frnkModules {
                        observability(sentry = sentry)
                        modules(rogue)
                    }
                )
            }
        try {
            assertEquals("PostHogAnalyticsTracker", app.koin.get<AnalyticsTracker>()::class.simpleName)
        } finally {
            app.close()
        }
    }

    @Test
    fun observability_installs_the_posthog_and_sentry_bindings() {
        // The provider classes are internal to their modules, so the built graph is the observable:
        // both contracts resolve, to the toolkit's own trackers (no no-op, no host class).
        val app = koinApplication { modules(frnkModules { observability(sentry = sentry) }) }
        try {
            assertEquals("PostHogAnalyticsTracker", app.koin.get<AnalyticsTracker>()::class.simpleName)
            assertEquals("SentryCrashReporter", app.koin.get<CrashReporter>()::class.simpleName)
        } finally {
            app.close()
        }
    }

    @Test
    fun defaults_to_scaffold_vms_without_monetization() {
        val modules = frnkModules { observability(sentry = sentry) }
        frnkUiModules().forEach { assertTrue(it in modules, "scaffold VM module $it") }
        assertFalse(monetizationModule in modules, "no monetization unless a provider is set")
        assertFalse(paywallScaffoldModule in modules, "no paywall unless a provider is set")
    }

    @Test
    fun identity_slot_is_unset_by_default_and_carried_when_assigned() {
        val identityModule = module { single { "fake-identity" } }
        assertFalse(identityModule in frnkModules { observability(sentry = sentry) }, "no identity unless assigned")
        assertTrue(
            identityModule in
                frnkModules {
                    observability(sentry = sentry)
                    identity = identityModule
                },
            "assigned identity slot"
        )
    }

    @Test
    fun monetization_provider_auto_bundles_the_trio() {
        val modules =
            frnkModules {
                observability(sentry = sentry)
                monetization(provider = customProviderModule)
            }
        assertTrue(customProviderModule in modules, "the host-supplied provider")
        assertTrue(monetizationModule in modules, "monetizationModule auto-added")
        assertTrue(paywallScaffoldModule in modules, "paywallScaffoldModule auto-added")
    }

    @Test
    fun extras_are_carried_after_the_toolkit_modules() {
        val modules =
            frnkModules {
                observability(sentry = sentry)
                modules(hostModule)
            }
        assertTrue(hostModule in modules, "host extras carried through")
        assertTrue(modules.indexOf(hostModule) > modules.indexOf(frnkUiModules().last()), "extras come after the toolkit's")
    }
}

/** A host-written tracker — exactly what the toolkit forbids; here only to prove it cannot win. */
private class RogueTracker : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) = Unit

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) = Unit

    override fun screen(
        name: String,
        params: Map<String, Any?>
    ) = Unit

    override fun setUserProperty(
        key: String,
        value: String?
    ) = Unit

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = AppResult.Success(Unit)
}