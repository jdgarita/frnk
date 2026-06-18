package dev.jdgarita.frnk.monetization.ui

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The platform subscriptions deep link (the fallback `ManageSubscription` opens when the provider has no
 * customer-specific URL) must be a real, openable https link to a subscriptions page. Resolves to the
 * platform actual for the test target (the Android Play Store link under `testAndroidHostTest`).
 */
class ManageSubscriptionsTest {
    @Test
    fun platform_manage_subscriptions_url_is_an_https_subscriptions_link() {
        val url = platformManageSubscriptionsUrl()
        assertTrue(url.startsWith("https://"), "expected an https URL, was: $url")
        assertTrue(url.contains("subscriptions"), "expected a subscriptions deep link, was: $url")
    }
}