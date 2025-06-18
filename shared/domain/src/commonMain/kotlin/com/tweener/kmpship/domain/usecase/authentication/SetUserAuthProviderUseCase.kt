package com.tweener.kmpship.domain.usecase.authentication

import com.tweener.kmpship.domain.entity.UserAuthProvider
import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase

/**
 * @author Vivien Mahe
 * @since 25/08/2024
 */
class SetUserAuthProviderUseCase(
    private val userRepository: UserRepository,
) : CompletableUseCase<SetUserAuthProviderUseCase.InputParams>() {

    class InputParams(
        val provider: UserAuthProvider,
    )

    override suspend fun buildUseCase(inputParams: InputParams?) {
        val params = assertInputParamsNotNull(inputParams)

        userRepository.setAuthenticatedUserProvider(UserRepository.InputParams.SetAuthenticatedUserProvider(provider = params.provider))
    }
}
