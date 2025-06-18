package dev.jdgarita.frnk.domain.usecase.user

import dev.jdgarita.frnk.domain.error.UserNotAuthenticatedException
import dev.jdgarita.frnk.domain.repository.UserRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase
import dev.jdgarita.frnk.domain.usecase.authentication.IsUserAuthenticatedUseCase
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
