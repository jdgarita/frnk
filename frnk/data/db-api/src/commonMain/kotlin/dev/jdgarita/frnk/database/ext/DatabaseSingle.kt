package dev.jdgarita.frnk.database.ext

import androidx.room.RoomDatabase
import dev.jdgarita.frnk.database.DatabaseFactory
import dev.jdgarita.frnk.database.SchemaUpgrade
import dev.jdgarita.frnk.database.open
import org.koin.core.module.Module

/**
 * Registers a `single<T>` that opens the host's Room database [T] through the Koin-provided
 * [DatabaseFactory], removing the hand-written
 * `single { get<DatabaseFactory>().open<MyDb>("my.db") }` boilerplate:
 *
 * ```
 * val myModule =
 *     module {
 *         databaseSingle<MyDb>("my.db")
 *     }
 * ```
 *
 * Pass [upgrade] for wipe-on-version-bump (see [SchemaUpgrade.WipeOnVersionBump]) and [configure]
 * to adjust the builder after the toolkit defaults (a different query context, callbacks,
 * `fallbackToDestructiveMigration`, …):
 *
 * ```
 * databaseSingle<MyDb>("my.db", SchemaUpgrade.WipeOnVersionBump(4)) { setQueryCoroutineContext(Dispatchers.Default) }
 * ```
 *
 * Requires a [DatabaseFactory] in the graph (install `databaseModule` from `:data-db-impl`);
 * [SchemaUpgrade.WipeOnVersionBump] additionally needs a `KeyValueStore` (install `prefsModule`).
 * The raw `single { ... }` form stays valid for anything this doesn't cover.
 */
inline fun <reified T : RoomDatabase> Module.databaseSingle(
    name: String,
    upgrade: SchemaUpgrade = SchemaUpgrade.None,
    noinline configure: RoomDatabase.Builder<T>.() -> Unit = {}
) {
    single { get<DatabaseFactory>().open<T>(name, upgrade, configure) }
}