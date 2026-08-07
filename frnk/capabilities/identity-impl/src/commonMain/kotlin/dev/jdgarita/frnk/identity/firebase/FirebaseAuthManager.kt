package dev.jdgarita.frnk.identity.firebase

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FirebaseAuthManager(
    private val auth: FirebaseAuthGateway = GitLiveFirebaseAuthGateway
) : AnonymousIdentityProvider {
    private val mutableUid = MutableStateFlow<String?>(null)

    override val uid: StateFlow<String?> = mutableUid.asStateFlow()

    override suspend fun ensureSignedIn(): AppResult<String, CommonError> =
        try {
            val resolvedUid = auth.currentUid ?: auth.signInAnonymously()
            mutableUid.value = resolvedUid
            AppResult.Success(resolvedUid)
        } catch (cancellation: CancellationException) {
            // todo add crashlytics
            throw cancellation
        } catch (_: Exception) {
            // todo add crashlytics
            AppResult.Failure(CommonError.Unknown)
        }

    override suspend fun idToken(forceRefresh: Boolean): AppResult<String, CommonError> {
        when (val signIn = ensureSignedIn()) {
            is AppResult.Failure -> return signIn
            is AppResult.Success -> Unit
        }
        return try {
            when (val token = auth.idToken(forceRefresh)) {
                null -> AppResult.Failure(CommonError.Unauthorized)
                else -> AppResult.Success(token)
            }
        } catch (cancellation: CancellationException) {
            // todo add crashlytics
            throw cancellation
        } catch (_: Exception) {
            // todo add crashlytics
            AppResult.Failure(CommonError.Unknown)
        }
    }
}