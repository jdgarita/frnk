package dev.jdgarita.frnk.domain.usecase.authentication

import dev.jdgarita.frnk.domain.entity.UserAuthProvider
import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.CompletableUseCase

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
