package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory
import org.koin.dsl.module

/** Provides default impls. Host apps still inject their schema directly into their own DI graph. */
val databaseModule = module {
    single<SqlDriverFactory> { defaultSqlDriverFactory() }
    single<KeyValueStore> { defaultKeyValueStore() }
}
