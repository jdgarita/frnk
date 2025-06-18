package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

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
