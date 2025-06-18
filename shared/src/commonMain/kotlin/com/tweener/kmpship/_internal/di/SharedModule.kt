package com.tweener.kmpship._internal.di

import com.revenuecat.purchases.kmp.Purchases
import com.tweener.alarmee.MobileAlarmeeService
import com.tweener.alarmee.createAlarmeeService
import com.tweener.firebase.analytics.FirebaseAnalyticsService
import com.tweener.firebase.crashlytics.FirebaseCrashlyticsService
import com.tweener.kmpship.BuildKonfig
import com.tweener.kmpship._internal.libs.LibrariesConfiguration
import com.tweener.kmpship.core._internal.di.coreModule
import com.tweener.kmpship.data._internal.di.dataModule
import com.tweener.kmpship.data._internal.libs.room.RoomConfiguration
import com.tweener.kmpship.domain._internal.di.domainModule
import com.tweener.kmpship.presentation._internal.di.presentationModule
import com.tweener.kmpship.presentation._internal.libs.alarmee.AlarmeeConfiguration
import com.tweener.kmpship.presentation._internal.libs.coil.CoilConfiguration
import com.tweener.kmpship.presentation._internal.libs.firebase.FirebaseConfiguration
import com.tweener.kmpship.presentation._internal.libs.napier.CrashlyticsAntilog
import com.tweener.kmpship.presentation._internal.libs.napier.NapierConfiguration
import com.tweener.kmpship.presentation._internal.libs.passage.PassageConfiguration
import com.tweener.kmpship.presentation._internal.os.thread.CoroutinesThreadDispatcher
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 10/02/2024
 */

val sharedModule = module {

    single { TimeZone.of(zoneId = "Europe/Paris") }

    singleOf(::CoroutinesThreadDispatcher)

    // Coil
    singleOf(::CoilConfiguration)

    // Firebase
    singleOf(::FirebaseCrashlyticsService)
    singleOf(::FirebaseAnalyticsService)
    single { FirebaseConfiguration(isDebug = BuildKonfig.DEBUG, firebaseCrashlyticsService = get(), firebaseAnalyticsService = get()) }

    // Napier
    singleOf(::CrashlyticsAntilog)
    single { NapierConfiguration(isDebug = BuildKonfig.DEBUG, passage = get(), crashlyticsAntilog = get(), firebaseCrashlyticsService = get()) }

    // Passage
    single { PassageConfiguration(serverClientId = BuildKonfig.GOOGLE_SIGN_IN_WEB_CLIENT_ID) }

    // Alarmee
    single<MobileAlarmeeService> { createAlarmeeService() as MobileAlarmeeService }
    singleOf(::AlarmeeConfiguration)

    // Room
    singleOf(::RoomConfiguration)

    // RevenueCat
    single { Purchases.sharedInstance }

    // Libraries
    singleOf(::LibrariesConfiguration)

    includes(dataModule(isDebug = BuildKonfig.DEBUG, versionName = BuildKonfig.VERSION_NAME))
    includes(domainModule)
    includes(presentationModule)
    includes(coreModule)

}
