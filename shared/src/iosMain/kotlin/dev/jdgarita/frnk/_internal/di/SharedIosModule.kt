package dev.jdgarita.frnk._internal.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import dev.jdgarita.frnk.BuildKonfig
import dev.jdgarita.frnk.data._internal.libs.room.RoomDatabaseHelper
import dev.jdgarita.frnk.data._internal.libs.room.RoomIosDatabaseHelper
import dev.jdgarita.frnk.presentation._internal.launcher.AppStoreSubscriptionLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.EmailComposerIosLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.EmailComposerLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.InAppReviewIosLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.InAppReviewLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.MobileStoreSubscriptionLauncher
import dev.jdgarita.frnk.presentation._internal.libs.revenuecat.RevenueCatConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.revenuecat.RevenueCatIosConfiguration
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
