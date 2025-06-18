package dev.jdgarita.frnk._internal.di

import com.revenuecat.purchases.kmp.Purchases
import com.tweener.alarmee.MobileAlarmeeService
import com.tweener.alarmee.createAlarmeeService
import com.tweener.firebase.analytics.FirebaseAnalyticsService
import com.tweener.firebase.crashlytics.FirebaseCrashlyticsService
import dev.jdgarita.frnk.BuildKonfig
import dev.jdgarita.frnk._internal.libs.LibrariesConfiguration
import dev.jdgarita.frnk.core._internal.di.coreModule
import dev.jdgarita.frnk.data._internal.di.dataModule
import dev.jdgarita.frnk.data._internal.libs.room.RoomConfiguration
import dev.jdgarita.frnk.domain._internal.di.domainModule
import dev.jdgarita.frnk.presentation._internal.di.presentationModule
import dev.jdgarita.frnk.presentation._internal.libs.alarmee.AlarmeeConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.coil.CoilConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.firebase.FirebaseConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.napier.CrashlyticsAntilog
import dev.jdgarita.frnk.presentation._internal.libs.napier.NapierConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.passage.PassageConfiguration
import dev.jdgarita.frnk.presentation._internal.os.thread.CoroutinesThreadDispatcher
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
