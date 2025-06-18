package dev.jdgarita.frnk.data._internal.di

import com.tweener.firebase.firestore.FirebaseFirestoreService
import com.tweener.firebase.functions.FirebaseFunctionsService
import com.tweener.firebase.remoteconfig.RemoteConfigService
import com.tweener.firebase.remoteconfig.datasource.FirebaseRemoteConfigDataSource
import dev.jdgarita.frnk.data._internal.libs.room.RoomConfiguration
import dev.jdgarita.frnk.data._internal.libs.usersession.UserSessionService
import dev.jdgarita.frnk.data.source.firebase.firestore.datasource.FirestoreSubscriptionsDiscountDataSource
import dev.jdgarita.frnk.data.source.firebase.firestore.datasource.FirestoreUserDataSource
import dev.jdgarita.frnk.data.source.local.datasource.LocalAppConfigurationDataSource
import dev.jdgarita.frnk.data.source.local.datasource.LocalStorageDataSource
import dev.jdgarita.frnk.data.source.revenuecat.datasource.RevenueCatDataSource
import dev.jdgarita.frnk.data.source.room.FrnkRoomDatabase
import dev.jdgarita.frnk.data.source.room.dao.RoomExampleDao
import dev.jdgarita.frnk.data.source.room.dao.RoomOtherClassDao
import dev.jdgarita.frnk.data.source.room.datasource.RoomExampleDataSource
import dev.jdgarita.frnk.data.source.room.datasource.RoomOtherClassDataSource
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 01/11/2023
 */

fun dataSourceModule(isDebug: Boolean, versionName: String) = module {

    // Room
    single<FrnkRoomDatabase> { get<RoomConfiguration>().getDatabase() }

    factory<RoomExampleDao> { get<FrnkRoomDatabase>().getExampleDao() }
    factory<RoomOtherClassDao> { get<FrnkRoomDatabase>().getOtherClassDao() }

    factoryOf(::RoomOtherClassDataSource)
    factoryOf(::RoomExampleDataSource)

    // Local
    factoryOf(::LocalStorageDataSource)
    singleOf(::LocalAppConfigurationDataSource)

    // Firestore
    singleOf(::FirebaseFirestoreService)
    single { FirestoreUserDataSource(firebaseFirestoreService = get(), versionName = versionName) }
    singleOf(::FirestoreSubscriptionsDiscountDataSource)

    // Remote Config
    single { RemoteConfigService(isDebug = isDebug) }
    singleOf(::FirebaseRemoteConfigDataSource)

    // Firebase Functions
    singleOf(::FirebaseFunctionsService)

    // RevenueCat
    singleOf(::RevenueCatDataSource)

    // UserSession
    singleOf(::UserSessionService)
}
