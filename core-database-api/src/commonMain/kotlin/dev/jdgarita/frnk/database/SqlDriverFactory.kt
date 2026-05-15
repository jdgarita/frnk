package dev.jdgarita.frnk.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * Contract for producing a SqlDriver bound to the host app's schema.
 * The toolkit does NOT own the schema — the host injects it.
 */
fun interface SqlDriverFactory {
    fun create(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        name: String,
    ): SqlDriver
}
