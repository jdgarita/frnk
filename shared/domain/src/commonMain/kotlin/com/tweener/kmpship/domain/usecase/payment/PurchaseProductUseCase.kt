package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 17/01/2025
 */
class PurchaseProductUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : SingleUseCase<PurchaseProductUseCase.InputParams, Result<Unit>>() {

    class InputParams(
        val productId: String
    )

    override suspend fun buildUseCase(inputParams: InputParams?): Result<Unit> {
        val params = assertInputParamsNotNull(inputParams)
        return accountSubscriptionRepository.purchaseProduct(AccountSubscriptionRepository.InputParams.PurchaseProduct(productId = params.productId))
    }
}
