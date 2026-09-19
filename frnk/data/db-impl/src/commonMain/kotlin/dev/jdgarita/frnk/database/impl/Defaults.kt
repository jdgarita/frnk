package dev.jdgarita.frnk.database.impl

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.jdgarita.frnk.database.DatabaseFactory
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SchemaUpgrade
import dev.jdgarita.frnk.database.shouldWipe
import kotlinx.coroutines.Dispatchers

/**
 * Platform database file operations. The open policy ([DefaultDatabaseFactory]) is common; only
 * these primitives differ per platform.
 */
internal interface DbPlatform {
    /**
     * Where the platform keeps the database file [name] — an absolute path, handed to the host's
     * builder as its location. The directory exists once this returns; the file need not.
     */
    fun databaseLocation(name: String): String

    fun databaseFileExists(name: String): Boolean

    /** Deletes the database file [name] plus its `-wal`/`-shm` sidecars, if present. */
    fun deleteDatabaseFiles(name: String)
}

/** expect — bound per platform. */
internal expect fun dbPlatform(): DbPlatform

/**
 * Default [DatabaseFactory]: opens the host's Room database at the platform location, honouring
 * [SchemaUpgrade], over the toolkit defaults — the bundled SQLite driver (one SQLite build on
 * every platform, so a query behaves the same on Android and iOS) and [Dispatchers.Default] as the
 * query context (the one background dispatcher common code can name; Room suspends over it, so
 * a DAO call never blocks the caller). The host's `configure` runs after both, so either can be
 * overridden per database.
 *
 * For [SchemaUpgrade.WipeOnVersionBump] it tracks the host's schema generation per database name
 * in [versionStore] (key `frnk.db.<name>.schema_version`): on a mismatch with an existing file it
 * deletes the file *before* Room sees it, then records the new version once the database is built
 * (so a crash mid-wipe can't strand a stale version). [SchemaUpgrade.None] just builds.
 *
 * @param versionStore the host's [KeyValueStore] (frnk's `prefsModule`); required only for
 *   [SchemaUpgrade.WipeOnVersionBump]. `null` is fine for [SchemaUpgrade.None].
 */
fun defaultDatabaseFactory(versionStore: KeyValueStore?): DatabaseFactory = DefaultDatabaseFactory(dbPlatform(), versionStore)

internal class DefaultDatabaseFactory(
    private val platform: DbPlatform,
    private val versionStore: KeyValueStore?
) : DatabaseFactory {
    override fun <T : RoomDatabase> open(
        name: String,
        upgrade: SchemaUpgrade,
        builder: (location: String) -> RoomDatabase.Builder<T>,
        configure: RoomDatabase.Builder<T>.() -> Unit
    ): T =
        when (upgrade) {
            SchemaUpgrade.None -> build(name, builder, configure)
            is SchemaUpgrade.WipeOnVersionBump -> buildWithWipe(name, upgrade.version, builder, configure)
        }

    private fun <T : RoomDatabase> build(
        name: String,
        builder: (location: String) -> RoomDatabase.Builder<T>,
        configure: RoomDatabase.Builder<T>.() -> Unit
    ): T =
        builder(platform.databaseLocation(name))
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .apply(configure)
            .build()

    private fun <T : RoomDatabase> buildWithWipe(
        name: String,
        version: Int,
        builder: (location: String) -> RoomDatabase.Builder<T>,
        configure: RoomDatabase.Builder<T>.() -> Unit
    ): T {
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
        val database = build(name, builder, configure)
        // Persist only after a successful build, and only when it changed (no stale-version window).
        if (persisted != version) {
            store.putString(key, version.toString())
        }
        return database
    }

    private fun versionKey(name: String): String = "frnk.db.$name.schema_version"
}