package dev.jdgarita.frnk.database.internal

import org.koin.dsl.module

/**
 * Driver creation is platform-specific — see androidMain/iosMain. The Koin
 * module here only binds the abstraction so api callers stay platform-pure.
 */
val databaseModule =
    module {
        // Platform modules provide the concrete SqlDriver + KeyValueStore.
    }
