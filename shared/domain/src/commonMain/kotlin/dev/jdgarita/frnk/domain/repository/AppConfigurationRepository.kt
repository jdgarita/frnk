package dev.jdgarita.frnk.domain.repository

import dev.jdgarita.frnk.domain.entity.AppConfiguration

/**
 * @author Vivien Mahe
 * @since 29/12/2023
 */
interface AppConfigurationRepository {

    sealed class OutputParams {
        data class GetAppConfiguration(val appConfiguration: AppConfiguration) : OutputParams()
    }

    suspend fun getAppConfiguration(): OutputParams.GetAppConfiguration
}
