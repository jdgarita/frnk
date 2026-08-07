package dev.jdgarita.frnk.identity.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

internal interface FirebaseAuthGateway {
    val currentUid: String?

    suspend fun signInAnonymously(): String

    suspend fun idToken(forceRefresh: Boolean): String?
}

internal object GitLiveFirebaseAuthGateway : FirebaseAuthGateway {
    override val currentUid: String?
        get() = Firebase.auth.currentUser?.uid

    override suspend fun signInAnonymously(): String =
        checkNotNull(
            Firebase.auth
                .signInAnonymously()
                .user
                ?.uid
        ) {
            "Firebase anonymous sign-in returned no user"
        }

    override suspend fun idToken(forceRefresh: Boolean): String? = Firebase.auth.currentUser?.getIdToken(forceRefresh)
}