package com.tweener.kmpship.domain.usecase.user

import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase

/**
 * @author Vivien Mahe
 * @since 20/11/2024
 */
class SetUserEmailVerifiedUseCase(
    private val userRepository: UserRepository,
) : CompletableUseCase<Nothing>() {

    override suspend fun buildUseCase(inputParams: Nothing?) {
        userRepository.setEmailVerified(UserRepository.InputParams.SetEmailVerified(isEmailVerified = true))
    }
}
