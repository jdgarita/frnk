package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val id: String,
    val email: String?,
    val isAnonymous: Boolean,
)

interface AuthService {
    val currentUser: Flow<AuthUser?>

    suspend fun signIn(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError>

    suspend fun signUp(
        email: String,
        password: String,
    ): AppResult<AuthUser, CommonError>

    suspend fun signOut(): AppResult<Unit, CommonError>
}
