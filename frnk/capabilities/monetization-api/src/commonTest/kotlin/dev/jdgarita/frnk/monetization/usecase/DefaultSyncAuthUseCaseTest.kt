package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.EntitlementStatus
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSyncAuthUseCaseTest {
    @Test
    fun successful_sign_in_identifies_the_uid_with_the_entitlement_manager() =
        runTest {
            val identity = ResultIdentityProvider(AppResult.Success("uid-42"))
            val entitlements = RecordingEntitlementManager()

            val result = DefaultSyncAuthUseCase(identity, entitlements).identify()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf("uid-42"), entitlements.identifiedUserIds)
        }

    @Test
    fun sign_in_failure_propagates_without_touching_the_entitlement_manager() =
        runTest {
            val identity = ResultIdentityProvider(AppResult.Failure(CommonError.Network))
            val entitlements = RecordingEntitlementManager()

            val result = DefaultSyncAuthUseCase(identity, entitlements).identify()

            assertEquals(AppResult.Failure(CommonError.Network), result)
            assertEquals(emptyList(), entitlements.identifiedUserIds)
        }

    @Test
    fun entitlement_identify_failure_propagates() =
        runTest {
            val identity = ResultIdentityProvider(AppResult.Success("uid-42"))
            val entitlements =
                RecordingEntitlementManager(identifyResult = AppResult.Failure(CommonError.Unknown))

            val result = DefaultSyncAuthUseCase(identity, entitlements).identify()

            assertEquals(AppResult.Failure(CommonError.Unknown), result)
            assertEquals(listOf("uid-42"), entitlements.identifiedUserIds)
        }
}

private class ResultIdentityProvider(
    private val signInResult: AppResult<String, CommonError>
) : AnonymousIdentityProvider {
    override val uid: StateFlow<String?> = MutableStateFlow(null)

    override suspend fun ensureSignedIn(): AppResult<String, CommonError> = signInResult

    override suspend fun idToken(forceRefresh: Boolean): AppResult<String, CommonError> = AppResult.Failure(CommonError.Unauthorized)
}

private class RecordingEntitlementManager(
    private val identifyResult: AppResult<Unit, CommonError> = AppResult.Success(Unit)
) : EntitlementManager {
    val identifiedUserIds = mutableListOf<String>()

    override val status: StateFlow<EntitlementStatus> = MutableStateFlow(EntitlementStatus.Free)
    override val isPro: StateFlow<Boolean> = MutableStateFlow(false)
    override val isGodMode: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun identify(userId: String): AppResult<Unit, CommonError> {
        identifiedUserIds += userId
        return identifyResult
    }

    override fun setGodMode(enabled: Boolean) = Unit

    override suspend fun refresh() = Unit

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun restorePurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}