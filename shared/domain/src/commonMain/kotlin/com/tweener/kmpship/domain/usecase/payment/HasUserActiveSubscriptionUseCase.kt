package com.tweener.kmpship.domain.usecase.payment

import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.usecase.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 25/02/2025
 */
class HasUserActiveSubscriptionUseCase(
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
) : StreamUseCase<Nothing, HasUserActiveSubscriptionUseCase.OutputParams>() {

    class OutputParams(
        val hasActiveSubscription: Boolean,
    )

    override suspend fun buildUseCase(inputParams: Nothing?): Flow<OutputParams> =
        accountSubscriptionRepository
            .isUserSubscribed()
            .map { OutputParams(hasActiveSubscription = it.isSubscribed) }
}
