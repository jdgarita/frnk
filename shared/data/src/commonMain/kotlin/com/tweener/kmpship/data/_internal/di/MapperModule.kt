package com.tweener.kmpship.data._internal.di

import com.tweener.kmpship.data.source.firebase.firestore.mapper.FirestorePlatformModelMapper
import com.tweener.kmpship.data.source.firebase.firestore.mapper.FirestoreUserModelMapper
import com.tweener.kmpship.data.source.firebase.remoteconfig.mapper.RemoteConfigFeatureFlagModelMapper
import com.tweener.kmpship.data.source.room.mapper.RoomLocalDateMapper
import com.tweener.kmpship.data.source.room.mapper.RoomLocalDateTimeMapper
import com.tweener.kmpship.data.source.local.mapper.LocalAuthProviderMapper
import com.tweener.kmpship.data.source.local.mapper.LocalThemeTypeModelMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 01/11/2023
 */

val mapperModule = module {

    // Remote Config
    factoryOf(::RemoteConfigFeatureFlagModelMapper)

    // Local
    factoryOf(::LocalThemeTypeModelMapper)
    factoryOf(::LocalAuthProviderMapper)

    // Room
    factoryOf(::RoomLocalDateMapper)
    factoryOf(::RoomLocalDateTimeMapper)

    // Firestore
    factoryOf(::FirestoreUserModelMapper)
    factoryOf(::FirestorePlatformModelMapper)
}
