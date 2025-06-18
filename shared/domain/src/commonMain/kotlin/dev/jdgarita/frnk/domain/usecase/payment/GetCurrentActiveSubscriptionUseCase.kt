package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.entity.AccountSubscription
import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

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
