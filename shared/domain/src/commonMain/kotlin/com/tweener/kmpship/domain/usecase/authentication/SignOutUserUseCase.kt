package com.tweener.kmpship.domain.usecase.authentication

import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase

/**
 * @author Vivien Mahe
 * @since 21/01/2024
 */
class SignOutUserUseCase(
    private val userRepository: UserRepository,
) : CompletableUseCase<Nothing>() {

    override suspend fun buildUseCase(inputParams: Nothing?) {
        userRepository.signOut()
    }
}
