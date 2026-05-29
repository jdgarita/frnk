package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.NoteStore
import dev.jdgarita.frnk.database.SqlDriverFactory
import dev.jdgarita.frnk.database.sql.FrnkDB
import org.koin.dsl.module

/**
 * Default persistence bindings. The toolkit owns the [FrnkDB] schema: the platform
 * [SqlDriverFactory] creates a driver for `FrnkDB.Schema`, and [NoteStore] (BACKLOG P1-1) exposes
 * typed access over it. Host apps can still install their own additional SQLDelight schema module.
 */
val databaseModule =
    module {
        single<SqlDriverFactory> { defaultSqlDriverFactory() }
        single<KeyValueStore> { defaultKeyValueStore() }
        single { FrnkDB(get<SqlDriverFactory>().create(FrnkDB.Schema, DATABASE_FILE_NAME)) }
        single<NoteStore> { SqlDelightNoteStore(get()) }
    }

private const val DATABASE_FILE_NAME = "frnk.db"
