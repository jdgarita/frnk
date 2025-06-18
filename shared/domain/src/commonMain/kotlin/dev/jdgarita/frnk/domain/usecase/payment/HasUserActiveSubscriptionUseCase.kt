package dev.jdgarita.frnk.domain.usecase.payment

import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.usecase.StreamUseCase
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
