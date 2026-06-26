package dev.jdgarita.frnk.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * Contract for producing a [SqlDriver] bound to the host app's schema. The toolkit owns NO schema —
 * the host injects it (`demo/shared`'s `DemoDB`/`demoNotesModule` is the worked example).
 *
 * [upgrade] selects how an existing on-disk database is reconciled with the current schema before the
 * driver opens it — see [SchemaUpgrade]. The default ([SchemaUpgrade.None]) is the plain
 * "open whatever is there" behavior; [SchemaUpgrade.WipeOnVersionBump] is the pre-launch
 * delete-and-recreate alternative to `.sqm` migrations.
 *
 * Was a `fun interface` before the wipe hook landed; it is now a regular interface because the SAM
 * can't carry the defaulted [upgrade] parameter. Construct it with `object : SqlDriverFactory`.
 */
interface SqlDriverFactory {
    fun create(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        name: String,
        upgrade: SchemaUpgrade = SchemaUpgrade.None
    ): SqlDriver
}