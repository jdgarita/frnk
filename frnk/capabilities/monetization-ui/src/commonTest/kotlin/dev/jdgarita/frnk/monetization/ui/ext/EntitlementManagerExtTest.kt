package dev.jdgarita.frnk.monetization.ui.ext

import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.EntitlementStatus
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.ui.platformManageSubscriptionsUrl
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EntitlementManagerExtTest {
    @Test
    fun returns_the_provider_management_url_when_available() =
        runTest {
            val manager = FakeEntitlementManager(managementUrlResult = AppResult.Success("https://rc.example/manage"))

            assertEquals("https://rc.example/manage", manager.manageSubscriptionsUrl())
        }

    @Test
    fun falls_back_to_the_platform_url_when_the_provider_has_none() =
        runTest {
            val manager = FakeEntitlementManager(managementUrlResult = AppResult.Success(null))

            assertEquals(platformManageSubscriptionsUrl(), manager.manageSubscriptionsUrl())
        }

    @Test
    fun falls_back_to_the_platform_url_on_provider_failure() =
        runTest {
            val manager = FakeEntitlementManager(managementUrlResult = AppResult.Failure(MonetizationError.StoreUnavailable))

            assertEquals(platformManageSubscriptionsUrl(), manager.manageSubscriptionsUrl())
        }
}

private class FakeEntitlementManager(
    private val managementUrlResult: AppResult<String?, MonetizationError>
) : EntitlementManager {
    override val status: StateFlow<EntitlementStatus> = MutableStateFlow(EntitlementStatus.Free)
    override val isPro: StateFlow<Boolean> = MutableStateFlow(false)
    override val isGodMode: StateFlow<Boolean> = MutableStateFlow(false)

    override fun setGodMode(enabled: Boolean) = Unit

    override suspend fun refresh() = Unit

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun restorePurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = managementUrlResult

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}
