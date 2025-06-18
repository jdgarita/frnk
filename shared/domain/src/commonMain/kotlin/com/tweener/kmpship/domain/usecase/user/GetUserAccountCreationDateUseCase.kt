package com.tweener.kmpship.domain.usecase.user

import com.tweener.kmpship.domain.error.UserNotAuthenticatedException
import com.tweener.kmpship.domain.repository.UserRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase
import com.tweener.kmpship.domain.usecase.authentication.IsUserAuthenticatedUseCase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 28/09/2024
 */
class GetUserAccountCreationDateUseCase(
    private val userRepository: UserRepository,
    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
) : SingleUseCase<Nothing, Result<GetUserAccountCreationDateUseCase.OutputParams>>() {

    class OutputParams(
        val date: LocalDateTime,
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Result<OutputParams> =
        when (isUserAuthenticatedUseCase.execute().first().authenticated) {
            true -> Result.success(OutputParams(date = userRepository.getAccountCreationDate().date))
            false -> Result.failure(UserNotAuthenticatedException())
        }
}
