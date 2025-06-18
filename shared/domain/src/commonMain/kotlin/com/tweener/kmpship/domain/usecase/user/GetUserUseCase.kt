package com.tweener.kmpship.domain.usecase.user

import com.tweener.kmpship.domain.entity.User
import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 07/11/2023
 */
class GetUserUseCase(
    private val userRepository: UserRepository,
) : StreamUseCase<Nothing, Result<GetUserUseCase.OutputParams>>() {

    class OutputParams(
        val user: User
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Flow<Result<OutputParams>> =
        userRepository
            .getAuthenticatedUser()
            .map { result -> result.map { OutputParams(user = it.user) } }
}
