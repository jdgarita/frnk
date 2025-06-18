package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.entity.Paywall
import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 16/01/2025
 */
class GetPaywallUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : SingleUseCase<Nothing, Result<GetPaywallUseCase.OutputParams>>() {

    class OutputParams(
        val paywall: Paywall,
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Result<OutputParams> =
        accountSubscriptionRepository
            .getPaywall()
            .map { OutputParams(paywall = it.paywall) }
}
