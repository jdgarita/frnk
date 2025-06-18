package dev.jdgarita.frnk.domain.usecase.config

import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.CompletableUseCase


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
