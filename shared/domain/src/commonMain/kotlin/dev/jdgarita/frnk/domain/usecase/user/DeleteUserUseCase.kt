package dev.jdgarita.frnk.domain.usecase.user

import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 26/01/2024
 */
class DeleteUserUseCase(
    private val userRepository: UserRepository,
) : SingleUseCase<DeleteUserUseCase.InputParams, Result<Unit>>() {

    class InputParams(
        val password: String? = null,
    )

    override suspend fun buildUseCase(inputParams: InputParams?): Result<Unit> {
        val params = assertInputParamsNotNull(inputParams)

        return try {
            userRepository.delete(UserRepository.InputParams.Delete(password = params.password))
            Result.success(Unit)
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}
