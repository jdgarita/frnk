package com.tweener.kmpship.data.repository

import com.tweener.kmpship.data.source.firebase.remoteconfig.model.RemoteConfigKey
import com.tweener.kmpship.data.source.firebase.remoteconfig.model.RemoteConfigModel
import com.tweener.kmpship.data.source.local.datasource.LocalAppConfigurationDataSource
import com.tweener.kmpship.domain.entity.AppConfiguration
import com.tweener.kmpship.domain.repository.AppConfigurationRepository
import com.tweener.firebase.remoteconfig.datasource.FirebaseRemoteConfigDataSource

/**
 * @author Vivien Mahe
 * @since 29/12/2023
 */
class AppConfigurationRepositoryImpl(
    private val localAppConfigurationDataSource: LocalAppConfigurationDataSource,
    private val remoteConfigDataSource: FirebaseRemoteConfigDataSource,
) : AppConfigurationRepository {

    override suspend fun getAppConfiguration(): AppConfigurationRepository.OutputParams.GetAppConfiguration {
        if (localAppConfigurationDataSource.appConfiguration == null) {
            val appRatingAskPeriodMonths = remoteConfigDataSource.getLong(key = RemoteConfigModel(key = RemoteConfigKey.APP_RATING_ASK_PERIOD_MONTHS).key.value, defaultValue = 3)

            localAppConfigurationDataSource.appConfiguration = AppConfiguration(
                appRatingAskPeriodMonths = appRatingAskPeriodMonths.toInt(),
                // TODO Add proper mapping for app configuration properties
            )
        }

        return AppConfigurationRepository.OutputParams.GetAppConfiguration(appConfiguration = localAppConfigurationDataSource.appConfiguration!!)
    }
}
