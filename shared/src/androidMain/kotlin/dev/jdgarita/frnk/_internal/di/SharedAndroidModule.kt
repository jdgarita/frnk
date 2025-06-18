package dev.jdgarita.frnk._internal.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.jdgarita.frnk.BuildKonfig
import dev.jdgarita.frnk.data._internal.libs.room.RoomAndroidDatabaseHelper
import dev.jdgarita.frnk.data._internal.libs.room.RoomDatabaseHelper
import dev.jdgarita.frnk.presentation._internal.launcher.EmailComposerAndroidLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.EmailComposerLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.GooglePlaySubscriptionLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.InAppReviewAndroidLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.InAppReviewLauncher
import dev.jdgarita.frnk.presentation._internal.launcher.MobileStoreSubscriptionLauncher
import dev.jdgarita.frnk.presentation._internal.libs.revenuecat.RevenueCatAndroidConfiguration
import dev.jdgarita.frnk.presentation._internal.libs.revenuecat.RevenueCatConfiguration
import com.tweener.passage.Passage
import com.tweener.passage.PassageAndroid
import org.koin.dsl.module
import java.util.Locale

/**
 * @author Vivien Mahe
 * @since 14/02/2024
 */

fun sharedAndroidModule(context: Context) = module {

    includes(sharedModule)

    single { context }
    single { Locale.getDefault() }

    // Launchers
    factory<MobileStoreSubscriptionLauncher> { GooglePlaySubscriptionLauncher(context = context) }
    factory<EmailComposerLauncher> { EmailComposerAndroidLauncher(context = context) }
    factory<InAppReviewLauncher> { InAppReviewAndroidLauncher(context = context) }

    // Multiplatform Settings
    single<ObservableSettings> { SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(context)) }
    single { get<ObservableSettings>().toFlowSettings() }

    // Passage
    single<Passage> { PassageAndroid(applicationContext = get()) }

    // Room
    factory<RoomDatabaseHelper> { RoomAndroidDatabaseHelper(context = get()) }

    // RevenueCat
    single<RevenueCatConfiguration> { RevenueCatAndroidConfiguration(isDebug = BuildKonfig.DEBUG, apiKey = BuildKonfig.REVENUECAT_ANDROID_API_KEY) }

}
