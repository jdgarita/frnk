package com.tweener.kmpship.domain.usecase.authentication

import com.tweener.kmpship.domain.entity.UserAuthProvider
import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase


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
