package dev.jdgarita.frnk.monetization.ui

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runAndroidComposeUiTest
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.mvi.LocalFrnkBackHandledByHost
import dev.jdgarita.frnk.ui.theme.FrnkLanguage
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.utils.AppResult
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [PaywallScreen]'s presentation modes and footer: dismissible by default (✕ and back close it); a
 * hard paywall (`dismissible = false`) has no ✕ and swallows back — standalone and under a host that
 * owns back (a `FrnkNavDisplay`) alike — yet a purchase still closes it; the legal footer becomes links
 * when the host passes URLs; the host's plan disclosure renders for the selected plan.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34], qualifiers = "w390dp-h844dp")
class PaywallScreenTest {
    private val theme = FrnkThemeConfig(language = FrnkLanguage.En)

    private val products =
        listOf(
            ProProduct("monthly", ProPlan.Monthly, "Monthly", "$4.99", "$4.99"),
            ProProduct("lifetime", ProPlan.Lifetime, "Lifetime", "$99.99")
        )

    private fun viewModel() =
        PaywallViewModel(
            paywallPurchaseUseCase =
                FakePaywallPurchaseUseCase(offerings = AppResult.Success(products), purchase = AppResult.Success(true)),
            analytics = FakeAnalytics(),
            syncAuthUseCase = FakeSyncAuthUseCase()
        )

    /** A stand-in for whatever sits under the paywall and would pop it (the NavDisplay, the activity). */
    private fun ComponentActivity.countBacksBeneath(): () -> Int {
        var backs = 0
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backs += 1
                }
            }
        )
        return { backs }
    }

    @Test
    fun by_default_the_close_button_and_back_dismiss_it() =
        runAndroidComposeUiTest<ComponentActivity> {
            val effects = mutableListOf<PaywallEffect>()
            val vm = viewModel()
            setContent { FrnkTheme(config = theme) { PaywallScreen(vm, "test", emptyList(), onEffect = { effects += it }) } }
            waitForIdle()

            onNodeWithContentDescription("Close").performClick()
            waitForIdle()
            assertEquals(1, effects.count { it == PaywallEffect.Dismiss })

            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()
            assertEquals(2, effects.count { it == PaywallEffect.Dismiss })
        }

    @Test
    fun a_hard_paywall_has_no_close_button_and_swallows_back() =
        runAndroidComposeUiTest<ComponentActivity> {
            val effects = mutableListOf<PaywallEffect>()
            val backsBeneath = runOnUiThread { activity!!.countBacksBeneath() }
            val vm = viewModel()
            setContent {
                FrnkTheme(config = theme) {
                    PaywallScreen(vm, "test", emptyList(), dismissible = false, onEffect = { effects += it })
                }
            }
            waitForIdle()

            onNodeWithContentDescription("Close").assertDoesNotExist()
            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()

            assertTrue(effects.none { it == PaywallEffect.Dismiss }, "back does not close a hard paywall")
            assertEquals(0, backsBeneath(), "back never reaches what is under the paywall")
            onNodeWithText("Continue").assertExists()
        }

    @Test
    fun a_hard_paywall_swallows_back_even_where_the_host_owns_back() =
        runAndroidComposeUiTest<ComponentActivity> {
            val effects = mutableListOf<PaywallEffect>()
            // Registered before the paywall composes, like a NavDisplay's pop handler.
            val backsBeneath = runOnUiThread { activity!!.countBacksBeneath() }
            val vm = viewModel()
            setContent {
                FrnkTheme(config = theme) {
                    CompositionLocalProvider(LocalFrnkBackHandledByHost provides true) {
                        PaywallScreen(vm, "test", emptyList(), dismissible = false, onEffect = { effects += it })
                    }
                }
            }
            waitForIdle()

            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()

            assertEquals(0, backsBeneath())
            assertTrue(effects.none { it == PaywallEffect.Dismiss })
        }

    @Test
    fun where_the_host_owns_back_a_dismissible_paywall_leaves_back_to_it() =
        runAndroidComposeUiTest<ComponentActivity> {
            val backsBeneath = runOnUiThread { activity!!.countBacksBeneath() }
            val vm = viewModel()
            setContent {
                FrnkTheme(config = theme) {
                    CompositionLocalProvider(LocalFrnkBackHandledByHost provides true) {
                        PaywallScreen(vm, "test", emptyList())
                    }
                }
            }
            waitForIdle()

            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()

            assertEquals(1, backsBeneath())
        }

    @Test
    fun a_purchase_still_closes_a_hard_paywall() =
        runAndroidComposeUiTest<ComponentActivity> {
            val effects = mutableListOf<PaywallEffect>()
            val vm = viewModel()
            setContent {
                FrnkTheme(config = theme) {
                    PaywallScreen(vm, "test", emptyList(), dismissible = false, onEffect = { effects += it })
                }
            }
            waitForIdle()

            onNodeWithText("Continue").performScrollTo().performClick()
            waitForIdle()

            assertTrue(effects.any { it is PaywallEffect.Purchased })
            assertEquals(1, effects.count { it == PaywallEffect.Dismiss })
        }

    @Test
    fun without_links_the_legal_footer_is_plain_text() =
        runAndroidComposeUiTest<ComponentActivity> {
            val vm = viewModel()
            setContent { FrnkTheme(config = theme) { PaywallScreen(vm, "test", emptyList()) } }
            waitForIdle()

            onNodeWithText("Terms · Privacy").performScrollTo().assertExists()
        }

    @Test
    fun with_links_terms_and_privacy_open_the_host_urls_and_report_the_tap() =
        runAndroidComposeUiTest<ComponentActivity> {
            val opened = mutableListOf<String>()
            val tapped = mutableListOf<PaywallLegalLink>()
            val uriHandler =
                object : UriHandler {
                    override fun openUri(uri: String) {
                        opened += uri
                    }
                }
            val vm = viewModel()
            setContent {
                FrnkTheme(config = theme) {
                    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                        PaywallScreen(
                            vm,
                            "test",
                            emptyList(),
                            legalLinks =
                                PaywallLegalLinks(
                                    termsUrl = "https://example.com/terms",
                                    privacyUrl = "https://example.com/privacy"
                                ),
                            onLegalLinkClick = { tapped += it }
                        )
                    }
                }
            }
            waitForIdle()

            onNodeWithText("Terms · Privacy").assertDoesNotExist()
            onNodeWithText("Terms").performScrollTo().performClick()
            onNodeWithText("Privacy").performScrollTo().performClick()
            waitForIdle()

            assertEquals(listOf(PaywallLegalLink.Terms, PaywallLegalLink.Privacy), tapped)
            assertEquals(listOf("https://example.com/terms", "https://example.com/privacy"), opened)
        }

    @Test
    fun the_plan_disclosure_follows_the_selected_plan() =
        runAndroidComposeUiTest<ComponentActivity> {
            val vm = viewModel()
            setContent {
                FrnkTheme(config = theme) {
                    PaywallScreen(
                        vm,
                        "test",
                        emptyList(),
                        planDisclosure = { product -> FrnkText(state = FrnkTextState.Body(text = "Terms for ${product.id}")) }
                    )
                }
            }
            waitForIdle()

            onNodeWithText("Terms for monthly").performScrollTo().assertExists()
            onNodeWithText("Lifetime").performClick()
            waitForIdle()
            onNodeWithText("Terms for lifetime").performScrollTo().assertExists()
            onNodeWithText("Terms for monthly").assertDoesNotExist()
        }
}