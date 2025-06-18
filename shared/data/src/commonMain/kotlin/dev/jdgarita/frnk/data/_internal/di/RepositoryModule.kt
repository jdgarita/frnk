package dev.jdgarita.frnk.data._internal.di

import dev.jdgarita.frnk.data.repository.AccountSubscriptionRepositoryImpl
import dev.jdgarita.frnk.data.repository.AppConfigurationRepositoryImpl
import dev.jdgarita.frnk.data.repository.FeatureFlagRepositoryImpl
import dev.jdgarita.frnk.data.repository.SettingsRepositoryImpl
import dev.jdgarita.frnk.data.repository.UserRepositoryImpl
import dev.jdgarita.frnk.domain.repository.AccountSubscriptionRepository
import dev.jdgarita.frnk.domain.repository.AppConfigurationRepository
import dev.jdgarita.frnk.domain.repository.FeatureFlagRepository
import dev.jdgarita.frnk.domain.repository.SettingsRepository
import dev.jdgarita.frnk.domain.repository.UserRepository
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 05/06/2023
 */
val repositoryModule = module {

    factory<SettingsRepository> { SettingsRepositoryImpl(firebaseCrashlyticsService = get(), firebaseAnalyticsService = get(), localThemeTypeModelMapper = get(), localStorageDataSource = get()) }

    factory<AppConfigurationRepository> { AppConfigurationRepositoryImpl(localAppConfigurationDataSource = get(), remoteConfigDataSource = get()) }

    factory<FeatureFlagRepository> { FeatureFlagRepositoryImpl(remoteConfigFeatureFlagModelMapper = get(), remoteConfigDataSource = get()) }

    factory<UserRepository> {
        UserRepositoryImpl(
            passage = get(),
            userSessionService = get(),
            userSyncScheduler = get(),
            roomDatabaseHelper = get(),
            localAuthProviderMapper = get(),
            firestoreUserModelMapper = get(),
            firestorePlatformModelMapper = get(),
            localStorageDataSource = get(),
            firestoreUserDataSource = get(),
            revenueCatDataSource = get(),
        )
    }

    factory<AccountSubscriptionRepository> {
        AccountSubscriptionRepositoryImpl(
            timeZone = get(),
            revenueCatPaywallProductMapper = get(),
            revenueCatAccountSubscriptionMapper = get(),
            firestoreSubscriptionsDiscountModelMapper = get(),
            revenueCatDataSource = get(),
            firestoreSubscriptionsDiscountDataSource = get(),
        )
    }
}
