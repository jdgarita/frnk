package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SchemaUpgrade
import dev.jdgarita.frnk.database.SqlDriverFactory
import dev.jdgarita.frnk.database.shouldWipe

/**
 * Platform database file + driver operations. The wipe policy ([DefaultSqlDriverFactory]) is common;
 * only these primitives differ per platform.
 */
internal interface DbPlatform {
    fun createDriver(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        name: String
    ): SqlDriver

    fun databaseFileExists(name: String): Boolean

    /** Deletes the database file [name] plus its `-wal`/`-shm` sidecars, if present. */
    fun deleteDatabaseFiles(name: String)
}

/** expect — bound per platform. */
internal expect fun dbPlatform(): DbPlatform

/**
 * Default [SqlDriverFactory]: opens the SQLDelight driver for the host's schema, honoring [SchemaUpgrade].
 *
 * For [SchemaUpgrade.WipeOnVersionBump] it tracks the host's schema generation per database name in
 * [versionStore] (key `frnk.db.<name>.schema_version`): on a mismatch with an existing file it deletes
 * the file *before* opening, then records the new version *after* a successful open (so a crash mid-wipe
 * can't strand a stale version). [SchemaUpgrade.None] just opens the file.
 *
 * @param versionStore the host's [KeyValueStore] (frnk's `prefsModule`); required only for
 *   [SchemaUpgrade.WipeOnVersionBump]. `null` is fine for [SchemaUpgrade.None].
 */
fun defaultSqlDriverFactory(versionStore: KeyValueStore?): SqlDriverFactory = DefaultSqlDriverFactory(dbPlatform(), versionStore)

internal class DefaultSqlDriverFactory(
    private val platform: DbPlatform,
    private val versionStore: KeyValueStore?
) : SqlDriverFactory {
    override fun create(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        name: String,
        upgrade: SchemaUpgrade
    ): SqlDriver =
        when (upgrade) {
            SchemaUpgrade.None -> platform.createDriver(schema, name)
            is SchemaUpgrade.WipeOnVersionBump -> createWithWipe(schema, name, upgrade.version)
        }

    private fun createWithWipe(
        schema: SqlSchema<QueryResult.Value<Unit>>,
        name: String,
        version: Int
    ): SqlDriver {
        val store =
            versionStore
                ?: error("SchemaUpgrade.WipeOnVersionBump requires a KeyValueStore — install prefsModule.")
        val key = versionKey(name)
        val persisted = store.getString(key)?.toIntOrNull()
        if (persisted != version) {
            if (shouldWipe(persisted, version, platform.databaseFileExists(name))) {
                platform.deleteDatabaseFiles(name)
            }
        }
        val driver = platform.createDriver(schema, name)
        // Persist only after a successful open, and only when it changed (m1: no stale-version window).
        if (persisted != version) {
            store.putString(key, version.toString())
        }
        return driver
    }

    private fun versionKey(name: String): String = "frnk.db.$name.schema_version"
}