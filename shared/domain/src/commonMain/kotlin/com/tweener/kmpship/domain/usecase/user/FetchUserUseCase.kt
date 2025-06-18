package com.tweener.kmpship.domain.usecase.user

import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase
import kotlinx.coroutines.flow.firstOrNull

/**
 * Gets the authenticated user and synchronizes all its data with the local database.
 *
 * @author Vivien Mahe
 * @since 30/07/2024
 */
class FetchUserUseCase(
    private val userRepository: UserRepository,
) : CompletableUseCase<Nothing>() {

    override suspend fun buildUseCase(inputParams: Nothing?) {
        userRepository
            .getAuthenticatedUser()
            .firstOrNull()
            ?.onSuccess {
                // TODO Add here your synchronization logic for the user's data.

                // Sync other user's attributes
                userRepository.synchronize(
                    UserRepository.InputParams.Synchronize(
                        isEmailVerified = it.user.isEmailVerified,
                    )
                )
            }
            ?.onFailure {
                // User is not authenticated, so we don't do anything
            }
    }
}
