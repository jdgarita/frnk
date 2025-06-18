package dev.jdgarita.frnk.data._internal.di

import dev.jdgarita.frnk.data.source.firebase.firestore.mapper.FirestorePlatformModelMapper
import dev.jdgarita.frnk.data.source.firebase.firestore.mapper.FirestoreUserModelMapper
import dev.jdgarita.frnk.data.source.firebase.remoteconfig.mapper.RemoteConfigFeatureFlagModelMapper
import dev.jdgarita.frnk.data.source.room.mapper.RoomLocalDateMapper
import dev.jdgarita.frnk.data.source.room.mapper.RoomLocalDateTimeMapper
import dev.jdgarita.frnk.data.source.local.mapper.LocalAuthProviderMapper
import dev.jdgarita.frnk.data.source.local.mapper.LocalThemeTypeModelMapper
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
