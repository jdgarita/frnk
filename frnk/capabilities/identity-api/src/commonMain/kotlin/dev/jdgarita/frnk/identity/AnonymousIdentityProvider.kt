package dev.jdgarita.frnk.identity

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.StateFlow

/** Provides the anonymous Firebase identity selected by the host application. */
interface AnonymousIdentityProvider {
    val uid: StateFlow<String?>

    suspend fun ensureSignedIn(): AppResult<String, CommonError>
}