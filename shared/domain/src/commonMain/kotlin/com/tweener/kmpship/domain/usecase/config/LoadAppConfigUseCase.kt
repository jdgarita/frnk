package com.tweener.kmpship.domain.usecase.config

import com.tweener.kmpship.domain.repository.AppConfigurationRepository
import com.tweener.kmpship.domain.usecase.CompletableUseCase

/**
 * @author Vivien Mahe
 * @since 09/12/2023
 */
class LoadAppConfigUseCase(
    private val appConfigurationRepository: AppConfigurationRepository,
) : CompletableUseCase<Nothing>() {

    override suspend fun buildUseCase(inputParams: Nothing?) {
        appConfigurationRepository.getAppConfiguration()

        // TODO Add here any usecase or repository for app configuration, like Remote Config for instance
    }
}
