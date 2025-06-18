package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

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
