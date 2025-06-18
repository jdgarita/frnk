package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 24/01/2025
 */
class RestorePurchaseUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : SingleUseCase<Nothing, Result<Unit>>() {

    override suspend fun buildUseCase(inputParams: Nothing?): Result<Unit> =
        accountSubscriptionRepository.restorePurchase()
}
