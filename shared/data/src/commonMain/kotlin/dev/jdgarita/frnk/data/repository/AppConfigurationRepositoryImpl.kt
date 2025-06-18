package dev.jdgarita.frnk.data.repository

import dev.jdgarita.frnk.data.source.firebase.remoteconfig.model.RemoteConfigKey
import dev.jdgarita.frnk.data.source.firebase.remoteconfig.model.RemoteConfigModel
import dev.jdgarita.frnk.data.source.local.datasource.LocalAppConfigurationDataSource
import dev.jdgarita.frnk.domain.entity.AppConfiguration
import dev.jdgarita.frnk.domain.repository.AppConfigurationRepository
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
