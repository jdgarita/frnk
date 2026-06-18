package dev.jdgarita.frnk.monetization.ui

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.usecase.PaywallPurchaseUseCase
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaywallViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val products =
        listOf(
            ProProduct("monthly", ProPlan.Monthly, "Monthly", "$4.99", "$4.99"),
            ProProduct("yearly", ProPlan.Yearly, "Yearly", "$39.99", "$3.33", hasFreeTrial = true, badge = "Save 33%")
        )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun attach_loads_offerings_and_defaults_to_yearly() =
        runTest(dispatcher) {
            val vm = PaywallViewModel(FakePaywallPurchaseUseCase(offerings = AppResult.Success(products)), FakeAnalytics())
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            assertEquals(2, vm.state.value.products.size)
            assertEquals("yearly", vm.state.value.selectedProductId)
            assertEquals(false, vm.state.value.isLoading)
        }

    @Test
    fun attach_tracks_paywall_viewed() =
        runTest(dispatcher) {
            val analytics = FakeAnalytics()
            val vm = PaywallViewModel(FakePaywallPurchaseUseCase(offerings = AppResult.Success(products)), analytics)
            vm.attach(PaywallArguments("settings"))
            runCurrent()
            assertTrue(analytics.tracked.contains(ToolkitEvent.PaywallViewed.key))
        }

    @Test
    fun purchase_success_dismisses() =
        runTest(dispatcher) {
            val vm =
                PaywallViewModel(
                    FakePaywallPurchaseUseCase(offerings = AppResult.Success(products), purchase = AppResult.Success(true)),
                    FakeAnalytics()
                )
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            val effects = mutableListOf<PaywallEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Purchase)
            runCurrent()

            assertTrue(effects.any { it is PaywallEffect.Dismiss })
            job.cancel()
        }

    @Test
    fun purchase_failure_emits_message_and_resets() =
        runTest(dispatcher) {
            val vm =
                PaywallViewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        purchase = AppResult.Failure(MonetizationError.StoreUnavailable)
                    ),
                    FakeAnalytics()
                )
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            val effects = mutableListOf<PaywallEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Purchase)
            runCurrent()

            assertTrue(effects.any { it is PaywallEffect.Message })
            assertEquals(false, vm.state.value.isPurchasing)
            job.cancel()
        }

    @Test
    fun restore_to_pro_dismisses() =
        runTest(dispatcher) {
            val vm =
                PaywallViewModel(
                    FakePaywallPurchaseUseCase(offerings = AppResult.Success(products), restore = AppResult.Success(true)),
                    FakeAnalytics()
                )
            vm.attach(PaywallArguments("settings"))
            runCurrent()
            val effects = mutableListOf<PaywallEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Restore)
            runCurrent()

            assertTrue(effects.any { it is PaywallEffect.Dismiss })
            job.cancel()
        }
}

private class FakePaywallPurchaseUseCase(
    private val offerings: AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList()),
    private val purchase: AppResult<Boolean, MonetizationError> = AppResult.Success(true),
    private val restore: AppResult<Boolean, MonetizationError> = AppResult.Success(true)
) : PaywallPurchaseUseCase {
    override suspend fun offerings() = offerings

    override suspend fun purchase(productId: String) = purchase

    override suspend fun restore() = restore
}

private class FakeAnalytics : AnalyticsTracker {
    val tracked = mutableListOf<String>()

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) {
        tracked += event.key
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) {
        tracked += name
    }

    override fun setUserProperty(
        key: String,
        value: String?
    ) = Unit
}