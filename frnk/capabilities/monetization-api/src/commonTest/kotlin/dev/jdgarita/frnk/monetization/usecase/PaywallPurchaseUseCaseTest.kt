package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.EntitlementStatus
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PaywallPurchaseUseCaseTest {
    private val products =
        listOf(
            ProProduct("yearly", ProPlan.Yearly, "Yearly", "$39.99", "$3.33", hasFreeTrial = true)
        )

    @Test
    fun offerings_delegates_to_entitlement_manager() =
        runTest {
            val success = AppResult.Success(products)
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(offerings = success))
            assertSame(success, useCase.offerings())
        }

    @Test
    fun offerings_propagates_failure() =
        runTest {
            val failure = AppResult.Failure(MonetizationError.NoOfferings)
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(offerings = failure))
            assertEquals(failure, useCase.offerings())
        }

    @Test
    fun purchase_delegates_and_forwards_product_id() =
        runTest {
            val manager = FakeManager(purchase = AppResult.Success(true))
            val useCase = DefaultPaywallPurchaseUseCase(manager)
            assertEquals(AppResult.Success(true), useCase.purchase("yearly"))
            assertEquals("yearly", manager.purchasedId)
        }

    @Test
    fun purchase_propagates_failure() =
        runTest {
            val failure = AppResult.Failure(MonetizationError.UserCancelled)
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(purchase = failure))
            assertEquals(failure, useCase.purchase("yearly"))
        }

    @Test
    fun restore_delegates_to_restore_purchases() =
        runTest {
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(restore = AppResult.Success(true)))
            assertEquals(AppResult.Success(true), useCase.restore())
        }

    @Test
    fun fetchMetadata_delegates_to_entitlement_manager() =
        runTest {
            val success = AppResult.Success(ProMetadata("Go Pro", "Unlock everything", emptyList()))
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(metadata = success))
            assertSame(success, useCase.fetchMetadata())
        }

    @Test
    fun fetchMetadata_propagates_failure() =
        runTest {
            val failure = AppResult.Failure(MonetizationError.NoOfferings)
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(metadata = failure))
            assertEquals(failure, useCase.fetchMetadata())
        }

    @Test
    fun restore_propagates_failure() =
        runTest {
            val failure = AppResult.Failure(MonetizationError.NetworkUnavailable)
            val useCase = DefaultPaywallPurchaseUseCase(FakeManager(restore = failure))
            assertEquals(failure, useCase.restore())
        }
}

private class FakeManager(
    private val offerings: AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList()),
    private val purchase: AppResult<Boolean, MonetizationError> = AppResult.Success(true),
    private val restore: AppResult<Boolean, MonetizationError> = AppResult.Success(true),
    private val metadata: AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
) : EntitlementManager {
    var purchasedId: String? = null
        private set

    override val status: StateFlow<EntitlementStatus> = MutableStateFlow(EntitlementStatus.Free)
    override val isPro: StateFlow<Boolean> = MutableStateFlow(false)
    override val isGodMode: StateFlow<Boolean> = MutableStateFlow(false)

    override fun setGodMode(enabled: Boolean) = Unit

    override suspend fun refresh() = Unit

    override suspend fun identify(userId: String) = Unit

    override suspend fun offerings() = offerings

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        purchasedId = productId
        return purchase
    }

    override suspend fun restorePurchases() = restore

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata() = metadata
}