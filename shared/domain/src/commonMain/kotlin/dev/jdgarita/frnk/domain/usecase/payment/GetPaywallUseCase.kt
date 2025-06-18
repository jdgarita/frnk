package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.entity.Paywall
import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

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
