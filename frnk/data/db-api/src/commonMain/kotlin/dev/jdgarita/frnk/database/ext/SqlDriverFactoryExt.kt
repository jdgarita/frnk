package dev.jdgarita.frnk.database.ext

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import dev.jdgarita.frnk.database.SqlDriverFactory
import org.koin.core.module.Module

/**
 * Registers a `single<T>` that builds a SQLDelight database [T] from the Koin-provided
 * [SqlDriverFactory], removing the hand-written
 * `single { MyDb(get<SqlDriverFactory>().create(MyDb.Schema, "my.db")) }` boilerplate:
 *
 * ```
 * val myModule =
 *     module {
 *         databaseSingle(MyDb.Schema, "my.db") { driver -> MyDb(driver) }
 *     }
 * ```
 *
 * Requires a [SqlDriverFactory] in the graph (install `databaseModule` from `:data-db-impl`).
 * The raw `single { ... }` form stays valid for anything this doesn't cover.
 */
inline fun <reified T : Any> Module.databaseSingle(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    name: String,
    crossinline create: (driver: SqlDriver) -> T
) {
    single { create(get<SqlDriverFactory>().create(schema, name)) }
}