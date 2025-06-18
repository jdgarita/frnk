package com.tweener.kmpship.domain.usecase.config

import com.tweener.kmpship.domain.repository.AppConfigurationRepository
import com.tweener.kmpship.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 29/12/2023
 */
class GetAppRatingAskPeriodMonthsUseCase(
    private val appConfigurationRepository: AppConfigurationRepository
) : SingleUseCase<Nothing, GetAppRatingAskPeriodMonthsUseCase.OutputParams>() {

    class OutputParams(
        val periodMonths: Int
    )

    override suspend fun buildUseCase(inputParams: Nothing?): OutputParams =
        OutputParams(periodMonths = appConfigurationRepository.getAppConfiguration().appConfiguration.appRatingAskPeriodMonths)
}
