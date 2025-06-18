package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

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
