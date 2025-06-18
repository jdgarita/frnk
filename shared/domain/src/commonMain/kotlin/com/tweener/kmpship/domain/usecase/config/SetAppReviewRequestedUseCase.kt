package com.tweener.kmpship.domain.usecase.config

import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase


/**
 * @author Vivien Mahe
 * @since 08/04/2025
 */
class SetAppReviewRequestedUseCase(
    private val userRepository: UserRepository,
) : CompletableUseCase<Nothing>() {

    override suspend fun buildUseCase(inputParams: Nothing?) {
        userRepository.setAppReviewRequested()
    }
}
