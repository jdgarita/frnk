package dev.jdgarita.frnk.domain.usecase.config

import dev.jdgarita.frnk.domain.repository.AppConfigurationRepository
import dev.jdgarita.frnk.domain.usecase.CompletableUseCase

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
