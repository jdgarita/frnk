package dev.jdgarita.frnk.backend.posthog

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PostHogAnalyticsTrackerTest {
    @Test
    fun `events screens properties and identity map onto the sdk calls`() =
        runTest {
            val gateway = RecordingPostHogGateway()
            val tracker = PostHogAnalyticsTracker(gateway)

            tracker.track(ToolkitEvent.PaywallViewed, mapOf("source" to "settings", "skipped" to null))
            tracker.trackCustom("card_saved", mapOf("images" to 2))
            tracker.screen("home", mapOf("tab" to "vault"))
            tracker.setUserProperty("card_count_bucket", "1_4")
            tracker.setUserProperty("dropped", null)
            tracker.identify("\$RCAnonymousID:abc")

            assertEquals(
                listOf<Pair<String, Map<String, Any>>>(
                    "paywall_viewed" to mapOf("source" to "settings"),
                    "card_saved" to mapOf("images" to 2)
                ),
                gateway.captured,
                "null params are dropped, nothing else is renamed"
            )
            assertEquals(listOf("home" to mapOf<String, Any>("tab" to "vault")), gateway.screens)
            assertEquals(listOf(mapOf<String, Any>("card_count_bucket" to "1_4")), gateway.personProperties, "a null value writes nothing")
            assertEquals("\$RCAnonymousID:abc", gateway.identified)
        }

    @Test
    fun `sdk failures degrade to a logged no-op`() =
        runTest {
            val tracker = PostHogAnalyticsTracker(ThrowingPostHogGateway())

            tracker.trackCustom("still_fine")
            tracker.screen("home")
            tracker.setUserProperty("k", "v")

            assertEquals(AppResult.Failure(IdentityError.Error), tracker.identify("uid"))
        }

    @Test
    fun `a blank api key is a configuration error, not a silent no-op`() {
        // Every host ships real analytics, so a missing key must fail where it is written — the
        // config — and name the fix, instead of binding a tracker that drops every event.
        val failure = assertFailsWith<IllegalArgumentException> { PostHogAnalyticsConfig(environment = "test", apiKey = " ") }
        assertTrue(failure.message.orEmpty().contains("apiKey"), "names the field")
    }

    @Test
    fun `the api key defaults to the toolkit-wide project so hosts never supply one`() {
        val config = PostHogAnalyticsConfig(environment = "test")
        assertEquals(FrnkPostHogProject.API_KEY, config.apiKey)
        assertEquals(FrnkPostHogProject.HOST, config.host)
    }

    @Test
    fun `a configured module binds the posthog tracker even when setup cannot run on the host`() {
        // No Koin androidContext here, so the Android actual cannot resolve an Application; the
        // module's runCatching keeps startKoin alive, which is the contract this pins.
        val config = PostHogAnalyticsConfig(environment = "test")
        val app = koinApplication { modules(postHogAnalyticsModule(config)) }
        try {
            assertTrue(app.koin.get<AnalyticsTracker>() is PostHogAnalyticsTracker)
        } finally {
            app.close()
        }
    }
}

private class RecordingPostHogGateway : PostHogGateway {
    val captured = mutableListOf<Pair<String, Map<String, Any>>>()
    val screens = mutableListOf<Pair<String, Map<String, Any>>>()
    val personProperties = mutableListOf<Map<String, Any>>()
    var identified: String? = null

    override fun capture(
        event: String,
        properties: Map<String, Any>
    ) {
        captured += event to properties
    }

    override fun screen(
        name: String,
        properties: Map<String, Any>
    ) {
        screens += name to properties
    }

    override fun identify(distinctId: String) {
        identified = distinctId
    }

    override fun setPersonProperties(properties: Map<String, Any>) {
        personProperties += properties
    }
}

private class ThrowingPostHogGateway : PostHogGateway {
    override fun capture(
        event: String,
        properties: Map<String, Any>
    ): Unit = error("sdk down")

    override fun screen(
        name: String,
        properties: Map<String, Any>
    ): Unit = error("sdk down")

    override fun identify(distinctId: String): Unit = error("sdk down")

    override fun setPersonProperties(properties: Map<String, Any>): Unit = error("sdk down")
}