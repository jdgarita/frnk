package dev.jdgarita.frnk.identity

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.StateFlow

/** Provides the anonymous Firebase identity selected by the host application. */
interface AnonymousIdentityProvider {
    val uid: StateFlow<String?>

    suspend fun ensureSignedIn(): AppResult<String, CommonError>

    /**
     * The current user's signed ID token (a JWT backends verify to derive the uid), signing in
     * anonymously first if needed. [forceRefresh] bypasses the SDK's internal token cache.
     */
    suspend fun idToken(forceRefresh: Boolean = false): AppResult<String, CommonError>
}