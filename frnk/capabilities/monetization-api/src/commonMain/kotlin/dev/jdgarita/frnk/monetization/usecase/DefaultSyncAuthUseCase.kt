package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import dev.jdgarita.frnk.utils.fold

class DefaultSyncAuthUseCase(
    private val identityProvider: AnonymousIdentityProvider,
    private val entitlementManager: EntitlementManager
) : SyncAuthUseCase {
    override suspend fun identify(): AppResult<Unit, CommonError> =
        identityProvider.ensureSignedIn().fold(
            onSuccess = { userId ->
                // todo add analytics
                entitlementManager.identify(userId)
            },
            onFailure = { error ->
                // todo add analytics
                AppResult.Failure(error)
            }
        )
}