package dev.jdgarita.frnk.domain.usecase.authentication

import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 17/10/2023
 */
class IsUserAuthenticatedUseCase(
    private val userRepository: UserRepository,
) : StreamUseCase<Nothing, IsUserAuthenticatedUseCase.OutputParams>() {

    class OutputParams(
        val authenticated: Boolean
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Flow<OutputParams> =
        userRepository
            .isAuthenticated()
            .map { it.authenticated }
            .catch { emit(false) }
            .map { OutputParams(authenticated = it) }
}
