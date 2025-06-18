package com.tweener.kmpship.data._internal.di

import com.tweener.firebase.firestore.FirebaseFirestoreService
import com.tweener.firebase.functions.FirebaseFunctionsService
import com.tweener.firebase.remoteconfig.RemoteConfigService
import com.tweener.firebase.remoteconfig.datasource.FirebaseRemoteConfigDataSource
import com.tweener.kmpship.data._internal.libs.room.RoomConfiguration
import com.tweener.kmpship.data._internal.libs.usersession.UserSessionService
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreSubscriptionsDiscountDataSource
import com.tweener.kmpship.data.source.firebase.firestore.datasource.FirestoreUserDataSource
import com.tweener.kmpship.data.source.local.datasource.LocalAppConfigurationDataSource
import com.tweener.kmpship.data.source.local.datasource.LocalStorageDataSource
import com.tweener.kmpship.data.source.revenuecat.datasource.RevenueCatDataSource
import com.tweener.kmpship.data.source.room.MyProjectRoomDatabase
import com.tweener.kmpship.data.source.room.dao.RoomExampleDao
import com.tweener.kmpship.data.source.room.dao.RoomOtherClassDao
import com.tweener.kmpship.data.source.room.datasource.RoomExampleDataSource
import com.tweener.kmpship.data.source.room.datasource.RoomOtherClassDataSource
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 01/11/2023
 */

fun dataSourceModule(isDebug: Boolean, versionName: String) = module {

    // Room
    single<MyProjectRoomDatabase> { get<RoomConfiguration>().getDatabase() }

    factory<RoomExampleDao> { get<MyProjectRoomDatabase>().getExampleDao() }
    factory<RoomOtherClassDao> { get<MyProjectRoomDatabase>().getOtherClassDao() }

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
