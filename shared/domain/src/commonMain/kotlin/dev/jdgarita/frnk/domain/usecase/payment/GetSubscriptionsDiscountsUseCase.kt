package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.entity.SubscriptionDiscount
import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 17/09/2024
 */
class GetSubscriptionsDiscountsUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : SingleUseCase<Nothing, GetSubscriptionsDiscountsUseCase.OutputParams>() {

    class OutputParams(
        val discounts: List<SubscriptionDiscount>,
    )

    override suspend fun buildUseCase(inputParams: Nothing?): OutputParams =
        accountSubscriptionRepository
            .fetchDiscounts()
            .discounts
            .filter { it.active } // Filter out discounts not active
            .let { OutputParams(discounts = it) }
}
