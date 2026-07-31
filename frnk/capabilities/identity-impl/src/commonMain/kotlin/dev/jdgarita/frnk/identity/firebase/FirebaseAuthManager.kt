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
            throw cancellation
        } catch (_: Exception) {
            AppResult.Failure(CommonError.Unknown)
        }
}