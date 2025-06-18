package com.tweener.kmpship._internal.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.kmpship.BuildKonfig
import com.tweener.kmpship.data._internal.libs.room.RoomDatabaseHelper
import com.tweener.kmpship.data._internal.libs.room.RoomIosDatabaseHelper
import com.tweener.kmpship.presentation._internal.launcher.AppStoreSubscriptionLauncher
import com.tweener.kmpship.presentation._internal.launcher.EmailComposerIosLauncher
import com.tweener.kmpship.presentation._internal.launcher.EmailComposerLauncher
import com.tweener.kmpship.presentation._internal.launcher.InAppReviewIosLauncher
import com.tweener.kmpship.presentation._internal.launcher.InAppReviewLauncher
import com.tweener.kmpship.presentation._internal.launcher.MobileStoreSubscriptionLauncher
import com.tweener.kmpship.presentation._internal.libs.revenuecat.RevenueCatConfiguration
import com.tweener.kmpship.presentation._internal.libs.revenuecat.RevenueCatIosConfiguration
import com.tweener.passage.Passage
import com.tweener.passage.PassageIos
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * @author Vivien Mahe
 * @since 16/02/2024
 */

val sharedIosModule = module {

    includes(sharedModule)

    // Launchers
    factory<MobileStoreSubscriptionLauncher> { AppStoreSubscriptionLauncher() }
    factory<EmailComposerLauncher> { EmailComposerIosLauncher() }
    factory<InAppReviewLauncher> { InAppReviewIosLauncher() }

    // Multiplatform Settings
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single { get<ObservableSettings>().toFlowSettings() }

    // Passage
    single<Passage> { PassageIos() }

    // Room
    factory<RoomDatabaseHelper> { RoomIosDatabaseHelper() }

    // RevenueCat
    single<RevenueCatConfiguration> { RevenueCatIosConfiguration(isDebug = BuildKonfig.DEBUG, apiKey = BuildKonfig.REVENUECAT_IOS_API_KEY) }
}
