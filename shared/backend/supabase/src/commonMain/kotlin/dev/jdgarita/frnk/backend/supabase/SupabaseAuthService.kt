package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AuthService
import dev.jdgarita.frnk.backend.AuthUser
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class SupabaseAuthService : AuthService {
    override val currentUser: Flow<AuthUser?> = flowOf(null)

    override suspend fun signIn(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError> = TODO("wire supabase-auth-kt")

    override suspend fun signUp(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError> = TODO("wire supabase-auth-kt")

    override suspend fun signOut(): AppResult<Unit, CommonError> = TODO("wire supabase-auth-kt")
}
