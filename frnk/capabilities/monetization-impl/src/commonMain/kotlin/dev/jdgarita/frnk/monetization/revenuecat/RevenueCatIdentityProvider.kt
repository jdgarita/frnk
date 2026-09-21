package dev.jdgarita.frnk.monetization.revenuecat

import com.revenuecat.purchases.kmp.Purchases
import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import dev.jdgarita.frnk.utils.PrintLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The one SDK read the provider needs, behind a seam so the provider is testable without `Purchases.sharedInstance`. */
internal interface RevenueCatIdentityGateway {
    /** Throws when the host has not configured `Purchases` yet. */
    val appUserId: String
}

internal object SharedInstanceIdentityGateway : RevenueCatIdentityGateway {
    override val appUserId: String
        get() = Purchases.sharedInstance.appUserID
}

/**
 * [AnonymousIdentityProvider] over the RevenueCat app user id.
 *
 * RevenueCat mints an anonymous id (`$RCAnonymousID:…`) the moment the host configures the SDK and
 * persists it across launches, so the id is a local read with no network round trip: [ensureSignedIn]
 * never suspends on I/O and can only fail when `Purchases` is unconfigured (a missing or placeholder
 * key — the documented graceful-degradation mode).
 *
 * The toolkit **never** calls `Purchases.logOut()`. An install that once identified the SDK with
 * another id (say a Firebase uid, as hosts did before this provider existed) keeps that id as its
 * app user id — identified, not anonymous — and this provider reports it verbatim, so entitlements
 * and any backend keyed on the id survive the switch without a migration.
 *
 * Handing this same id to `EntitlementProvider.identify` is a no-op: the RevenueCat provider skips
 * the network `logIn` when the id already matches, so `DefaultSyncAuthUseCase` stays a pure fan-out
 * to the analytics and crash sinks.
 */
internal class RevenueCatIdentityProvider(
    private val gateway: RevenueCatIdentityGateway = SharedInstanceIdentityGateway
) : AnonymousIdentityProvider {
    private val mutableUid = MutableStateFlow<String?>(null)

    override val uid: StateFlow<String?> = mutableUid.asStateFlow()

    override suspend fun ensureSignedIn(): AppResult<String, CommonError> =
        runCatching { gateway.appUserId }
            .fold(
                onSuccess = { id ->
                    mutableUid.value = id
                    AppResult.Success(id)
                },
                onFailure = {
                    PrintLogger.w(TAG, "app user id unavailable, Purchases not configured: ${it.message}")
                    AppResult.Failure(CommonError.Unknown)
                }
            )

    private companion object {
        const val TAG = "RevenueCatIdentity"
    }
}