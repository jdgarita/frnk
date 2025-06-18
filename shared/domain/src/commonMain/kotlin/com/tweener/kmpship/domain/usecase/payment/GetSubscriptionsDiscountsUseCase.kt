package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.entity.SubscriptionDiscount
import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

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
