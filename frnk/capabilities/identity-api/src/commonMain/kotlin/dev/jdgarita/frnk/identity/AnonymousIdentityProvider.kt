package dev.jdgarita.frnk.identity

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.StateFlow

/**
 * Produces the app's anonymous identity — one stable id per install, with no account behind it.
 *
 * Which system mints the id is the impl's business: `revenueCatModule` (`:monetization-impl`) binds
 * it to the RevenueCat app user id, `firebaseIdentityModule` (`:identity-impl`) to a Firebase
 * anonymous user. Consumers only ever see the string. Whether that string can also serve as a
 * credential a backend trusts is a host concern, deliberately outside this contract.
 */
interface AnonymousIdentityProvider {
    /** The last resolved id, `null` until [ensureSignedIn] has succeeded once. */
    val uid: StateFlow<String?>

    /**
     * Resolves the id, creating it on first use where the provider has to (a network round trip
     * for Firebase; a local read for RevenueCat). Failure means the provider is unconfigured or
     * unreachable, and [uid] is left as it was.
     */
    suspend fun ensureSignedIn(): AppResult<String, CommonError>
}