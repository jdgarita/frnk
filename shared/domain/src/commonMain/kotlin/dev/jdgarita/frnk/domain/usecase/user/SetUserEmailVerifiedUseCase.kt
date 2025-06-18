package dev.jdgarita.frnk.domain.usecase.user

import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.CompletableUseCase

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
