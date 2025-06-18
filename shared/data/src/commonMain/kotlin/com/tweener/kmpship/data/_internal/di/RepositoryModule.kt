package com.tweener.kmpship.data._internal.di

import com.tweener.kmpship.data.repository.AccountSubscriptionRepositoryImpl
import com.tweener.kmpship.data.repository.AppConfigurationRepositoryImpl
import com.tweener.kmpship.data.repository.FeatureFlagRepositoryImpl
import com.tweener.kmpship.data.repository.SettingsRepositoryImpl
import com.tweener.kmpship.data.repository.UserRepositoryImpl
import com.tweener.kmpship.domain.repository.AccountSubscriptionRepository
import com.tweener.kmpship.domain.repository.AppConfigurationRepository
import com.tweener.kmpship.domain.repository.FeatureFlagRepository
import com.tweener.kmpship.domain.repository.SettingsRepository
import com.tweener.kmpship.domain.repository.UserRepository
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
