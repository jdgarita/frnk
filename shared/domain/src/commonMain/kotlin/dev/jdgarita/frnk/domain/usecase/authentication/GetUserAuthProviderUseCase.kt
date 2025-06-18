package dev.jdgarita.frnk.domain.usecase.authentication

import dev.jdgarita.frnk.domain.entity.UserAuthProvider
import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase


/**
 * @author Vivien Mahe
 * @since 16/08/2024
 */
class GetUserAuthProviderUseCase(
    private val userRepository: UserRepository,
) : SingleUseCase<Nothing, Result<GetUserAuthProviderUseCase.OutputParams>>() {

    class OutputParams(
        val provider: UserAuthProvider,
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Result<OutputParams> =
        userRepository
            .getAuthenticatedUserProvider()
            .map { OutputParams(provider = it.provider) }
}
