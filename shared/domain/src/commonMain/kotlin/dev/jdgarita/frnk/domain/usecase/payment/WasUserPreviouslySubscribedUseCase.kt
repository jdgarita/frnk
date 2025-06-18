package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 22/01/2025
 */
class WasUserPreviouslySubscribedUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : SingleUseCase<Nothing, WasUserPreviouslySubscribedUseCase.OutputParams>() {

    class OutputParams(
        val wasSubscribed: Boolean,
    )

    override suspend fun buildUseCase(inputParams: Nothing?): OutputParams {
        val wasSubscribed = accountSubscriptionRepository.wasUserPreviouslySubscribed().userWasAlreadySubscribed
        return OutputParams(wasSubscribed = wasSubscribed)
    }
}
