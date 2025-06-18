package com.tweener.kmpship.domain.repository

import com.tweener.kmpship.domain.entity.User
import com.tweener.kmpship.domain.entity.UserAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 17/10/2023
 */
interface UserRepository {

    sealed class InputParams {
        data class SetAuthenticatedUserProvider(val provider: UserAuthProvider) : InputParams()
        data class SetLastAskForAppReviewDate(val date: LocalDateTime) : InputParams()
        data class SetEmailVerified(val isEmailVerified: Boolean) : InputParams()
        data class Synchronize(val isEmailVerified: Boolean) : InputParams()
        data class Delete(val password: String? = null) : InputParams()
    }

    sealed class OutputParams {
        data class GetAuthenticatedUserProvider(val provider: UserAuthProvider) : OutputParams()
        data class IsAuthenticated(val authenticated: Boolean) : OutputParams()
        data class GetAuthenticatedUser(val user: User) : OutputParams()
        data class IsAppReviewRequested(val requested: Boolean) : OutputParams()
        data class GetLastAskForAppReviewDate(val date: LocalDateTime?) : OutputParams()
        data class GetAccountCreationDate(val date: LocalDateTime) : OutputParams()
        data class IsEmailVerified(val isEmailVerified: Boolean) : OutputParams()
        data class IsNotificationsPermissionAsked(val isAsked: Boolean) : OutputParams()
        data class IsFirstTimePaywallShown(val shown: Boolean) : OutputParams()
    }

    suspend fun isAuthenticated(): Flow<OutputParams.IsAuthenticated>

    suspend fun getAuthenticatedUser(): Flow<Result<OutputParams.GetAuthenticatedUser>>

    suspend fun setAuthenticatedUserProvider(inputParams: InputParams.SetAuthenticatedUserProvider)

    suspend fun getAuthenticatedUserProvider(): Result<OutputParams.GetAuthenticatedUserProvider>

    suspend fun setAppReviewRequested()

    suspend fun isAppReviewRequested(): Flow<OutputParams.IsAppReviewRequested>

    suspend fun setLastAskForAppReviewDate(inputParams: InputParams.SetLastAskForAppReviewDate)

    suspend fun getLastAskForAppReviewDate(): OutputParams.GetLastAskForAppReviewDate

    suspend fun getAccountCreationDate(): OutputParams.GetAccountCreationDate

    suspend fun setEmailVerified(inputParams: InputParams.SetEmailVerified)

    suspend fun isEmailVerified(): Flow<OutputParams.IsEmailVerified>

    suspend fun setNotificationsPermissionAsked()

    suspend fun isNotificationsPermissionAsked(): Flow<OutputParams.IsNotificationsPermissionAsked>

    suspend fun setFirstTimePaywallShown()

    suspend fun isFirstTimePaywallShown(): Flow<OutputParams.IsFirstTimePaywallShown>

    suspend fun synchronize(inputParams: InputParams.Synchronize)

    suspend fun delete(inputParams: InputParams.Delete): Result<Unit>

    suspend fun signOut()
}
