package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AppResult
import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.AuthUser
import dev.jdgarita.frnk.backend.CommonError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FirebaseAuthService : AuthService {
    override val currentUser: Flow<AuthUser?> = flowOf(null)

    override suspend fun signIn(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError> = TODO("wire dev.gitlive:firebase-auth")

    override suspend fun signUp(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError> = TODO("wire dev.gitlive:firebase-auth")

    override suspend fun signOut(): AppResult<Unit, CommonError> = TODO("wire dev.gitlive:firebase-auth")
}
