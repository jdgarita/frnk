package dev.jdgarita.frnk.monetization.ui

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProBenefit
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.usecase.PaywallPurchaseUseCase
import dev.jdgarita.frnk.monetization.usecase.SyncAuthUseCase
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.stringPaywallAlreadyOwnedRestoring
import dev.jdgarita.frnk.ui.theme.stringPaywallIdentityError
import dev.jdgarita.frnk.ui.theme.stringPaywallNothingToRestore
import dev.jdgarita.frnk.ui.theme.stringPaywallRestored
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaywallViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val products =
        listOf(
            ProProduct("monthly", ProPlan.Monthly, "Monthly", "$4.99", "$4.99"),
            ProProduct("yearly", ProPlan.Yearly, "Yearly", "$39.99", "$3.33", hasFreeTrial = true, badge = "Save 33%")
        )

    private val metadata = ProMetadata("Go Pro", "Unlock everything", listOf(ProBenefit("SCANS", "Unlimited scans")))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        useCase: FakePaywallPurchaseUseCase,
        analytics: AnalyticsTracker = FakeAnalytics(),
        syncAuth: SyncAuthUseCase = FakeSyncAuthUseCase()
    ) = PaywallViewModel(useCase, analytics, syncAuth)

    @Test
    fun attach_loads_offerings_and_defaults_to_yearly() =
        runTest(dispatcher) {
            val vm = viewModel(FakePaywallPurchaseUseCase(offerings = AppResult.Success(products)))
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            assertEquals(2, vm.state.value.products.size)
            assertEquals("yearly", vm.state.value.selectedProductId)
            assertEquals(false, vm.state.value.isLoading)
        }

    @Test
    fun attach_applies_metadata_alongside_products() =
        runTest(dispatcher) {
            val vm =
                viewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        metadata = AppResult.Success(metadata)
                    )
                )
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            assertEquals("Go Pro", vm.state.value.title)
            assertEquals("Unlock everything", vm.state.value.subtitle)
            assertEquals(metadata.benefits, vm.state.value.benefits)
            assertEquals(2, vm.state.value.products.size)
            assertEquals(false, vm.state.value.isLoading)
        }

    @Test
    fun metadata_failure_discards_products_and_emits_single_message() =
        runTest(dispatcher) {
            val vm =
                viewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        metadata = AppResult.Failure(MonetizationError.NoOfferings)
                    )
                )
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()

            assertTrue(
                vm.state.value.products
                    .isEmpty()
            )
            assertEquals(false, vm.state.value.isLoading)
            assertEquals(1, effects.count { it is PaywallEffect.Message })
            job.cancel()
        }

    @Test
    fun attach_tracks_paywall_viewed() =
        runTest(dispatcher) {
            val analytics = FakeAnalytics()
            val vm = viewModel(FakePaywallPurchaseUseCase(offerings = AppResult.Success(products)), analytics)
            vm.attach(PaywallArguments("settings"))
            runCurrent()
            assertTrue(analytics.tracked.contains(ToolkitEvent.PaywallViewed.key))
        }

    @Test
    fun attach_silent_sync_restores_pro_and_dismisses() =
        runTest(dispatcher) {
            val useCase =
                FakePaywallPurchaseUseCase(
                    offerings = AppResult.Success(products),
                    sync = AppResult.Success(true)
                )
            val vm = viewModel(useCase)
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()

            assertEquals(1, useCase.syncCallCount)
            assertTrue(
                effects.any {
                    it is PaywallEffect.Message && it.text == FrnkStringSource.Token(stringPaywallRestored)
                }
            )
            assertTrue(effects.any { it is PaywallEffect.Dismiss })
            job.cancel()
        }

    @Test
    fun attach_silent_sync_failure_is_silent() =
        runTest(dispatcher) {
            val useCase =
                FakePaywallPurchaseUseCase(
                    offerings = AppResult.Success(products),
                    sync = AppResult.Failure(MonetizationError.StoreUnavailable)
                )
            val vm = viewModel(useCase)
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()

            assertTrue(effects.none { it is PaywallEffect.Message })
            assertTrue(effects.none { it is PaywallEffect.Dismiss })
            job.cancel()
        }

    @Test
    fun attach_identity_failure_skips_silent_sync() =
        runTest(dispatcher) {
            val useCase =
                FakePaywallPurchaseUseCase(
                    offerings = AppResult.Success(products),
                    sync = AppResult.Success(true)
                )
            val vm =
                viewModel(
                    useCase,
                    syncAuth = FakeSyncAuthUseCase(AppResult.Failure(CommonError.Unknown))
                )
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()

            assertEquals(0, useCase.syncCallCount)
            assertTrue(effects.none { it is PaywallEffect.Dismiss })
            job.cancel()
        }

    @Test
    fun purchase_success_dismisses() =
        runTest(dispatcher) {
            val vm =
                viewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        purchase = AppResult.Success(true)
                    )
                )
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            val effects = mutableListOf<UiEffect>()
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
                viewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        purchase = AppResult.Failure(MonetizationError.StoreUnavailable)
                    )
                )
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Purchase)
            runCurrent()

            assertTrue(effects.any { it is PaywallEffect.Message })
            assertEquals(false, vm.state.value.isPurchasing)
            job.cancel()
        }

    @Test
    fun purchase_already_owned_falls_through_to_restore() =
        runTest(dispatcher) {
            val useCase =
                FakePaywallPurchaseUseCase(
                    offerings = AppResult.Success(products),
                    purchase = AppResult.Failure(MonetizationError.AlreadyOwned),
                    restore = AppResult.Success(true)
                )
            val vm = viewModel(useCase)
            vm.attach(PaywallArguments("home_topbar"))
            runCurrent()
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Purchase)
            runCurrent()

            assertEquals(1, useCase.restoreCallCount)
            assertTrue(
                effects.any {
                    it is PaywallEffect.Message && it.text == FrnkStringSource.Token(stringPaywallAlreadyOwnedRestoring)
                }
            )
            assertTrue(effects.any { it is PaywallEffect.Dismiss })
            job.cancel()
        }

    @Test
    fun restore_to_pro_dismisses() =
        runTest(dispatcher) {
            val vm =
                viewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        restore = AppResult.Success(true)
                    )
                )
            vm.attach(PaywallArguments("settings"))
            runCurrent()
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Restore)
            runCurrent()

            assertTrue(effects.any { it is PaywallEffect.Dismiss })
            job.cancel()
        }

    @Test
    fun restore_with_nothing_to_restore_emits_token_message() =
        runTest(dispatcher) {
            val vm =
                viewModel(
                    FakePaywallPurchaseUseCase(
                        offerings = AppResult.Success(products),
                        restore = AppResult.Success(false)
                    )
                )
            vm.attach(PaywallArguments("settings"))
            runCurrent()
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Restore)
            runCurrent()

            assertTrue(
                effects.any {
                    it is PaywallEffect.Message && it.text == FrnkStringSource.Token(stringPaywallNothingToRestore)
                }
            )
            job.cancel()
        }

    @Test
    fun restore_identity_failure_stops_before_the_store() =
        runTest(dispatcher) {
            val useCase =
                FakePaywallPurchaseUseCase(
                    offerings = AppResult.Success(products),
                    restore = AppResult.Success(true)
                )
            val vm =
                viewModel(
                    useCase,
                    syncAuth = FakeSyncAuthUseCase(AppResult.Failure(CommonError.Unknown))
                )
            vm.attach(PaywallArguments("settings"))
            runCurrent()
            val effects = mutableListOf<UiEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(PaywallIntent.Restore)
            runCurrent()

            assertEquals(0, useCase.restoreCallCount)
            assertFalse(vm.state.value.isRestoring)
            assertTrue(
                effects.any {
                    it is PaywallEffect.Message && it.text == FrnkStringSource.Token(stringPaywallIdentityError)
                }
            )
            job.cancel()
        }

    @Test
    fun restore_sets_is_restoring_while_in_flight_and_clears_it_after() =
        runTest(dispatcher) {
            val gate = CompletableDeferred<Unit>()
            val useCase =
                FakePaywallPurchaseUseCase(
                    offerings = AppResult.Success(products),
                    restore = AppResult.Success(true),
                    restoreGate = gate
                )
            val vm = viewModel(useCase)
            vm.attach(PaywallArguments("settings"))
            runCurrent()

            vm.send(PaywallIntent.Restore)
            runCurrent()
            assertTrue(vm.state.value.isRestoring)

            gate.complete(Unit)
            runCurrent()
            assertFalse(vm.state.value.isRestoring)
        }
}

private class FakePaywallPurchaseUseCase(
    private val offerings: AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList()),
    private val purchase: AppResult<Boolean, MonetizationError> = AppResult.Success(true),
    private val restore: AppResult<Boolean, MonetizationError> = AppResult.Success(true),
    private val sync: AppResult<Boolean, MonetizationError> = AppResult.Success(false),
    private val metadata: AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY),
    private val restoreGate: CompletableDeferred<Unit>? = null
) : PaywallPurchaseUseCase {
    var restoreCallCount = 0
        private set

    var syncCallCount = 0
        private set

    override suspend fun offerings() = offerings

    override suspend fun fetchMetadata() = metadata

    override suspend fun purchase(productId: String) = purchase

    override suspend fun restore(): AppResult<Boolean, MonetizationError> {
        restoreCallCount++
        restoreGate?.await()
        return restore
    }

    override suspend fun sync(): AppResult<Boolean, MonetizationError> {
        syncCallCount++
        return sync
    }
}

private class FakeSyncAuthUseCase(
    private val result: AppResult<Unit, CommonError> = AppResult.Success(Unit)
) : SyncAuthUseCase {
    var identifyCount = 0
        private set

    override suspend fun identify(): AppResult<Unit, CommonError> {
        identifyCount++
        return result
    }
}

private class FakeAnalytics : AnalyticsTracker {
    val tracked = mutableListOf<String>()

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = AppResult.Success(Unit)

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