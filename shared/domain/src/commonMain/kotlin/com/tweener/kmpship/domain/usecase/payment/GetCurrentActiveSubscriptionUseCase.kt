package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.entity.AccountSubscription
import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
class GetCurrentActiveSubscriptionUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : SingleUseCase<GetCurrentActiveSubscriptionUseCase.InputParams, Result<GetCurrentActiveSubscriptionUseCase.OutputParams>>() {

    class InputParams(
        val forceFetch: Boolean,
    )

    class OutputParams(
        val accountSubscription: AccountSubscription,
    )

    override suspend fun buildUseCase(inputParams: InputParams?): Result<OutputParams> {
        val params = assertInputParamsNotNull(inputParams)

        return accountSubscriptionRepository
            .getCurrentSubscription(AccountSubscriptionRepository.InputParams.GetCurrentSubscription(forceFetch = params.forceFetch))
            .map { OutputParams(accountSubscription = it.accountSubscription) }
    }
}
