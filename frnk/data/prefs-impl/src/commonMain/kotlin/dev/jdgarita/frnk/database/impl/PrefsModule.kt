package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import org.koin.dsl.module

/**
 * Default key-value persistence binding: [KeyValueStore] over `multiplatform-settings`
 * (SharedPreferences on Android, NSUserDefaults on iOS). Split out of `databaseModule` at
 * restructure Stage 4 — install this alongside (or without) the SQL driver wiring; the typed
 * `Preference<T>` layer (`:data-prefs-api`) works over whatever this binds.
 */
val prefsModule =
    module {
        single<KeyValueStore> { defaultKeyValueStore() }
    }