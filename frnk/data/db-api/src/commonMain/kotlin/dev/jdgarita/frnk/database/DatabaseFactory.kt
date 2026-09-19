package dev.jdgarita.frnk.database

import androidx.room.RoomDatabase

/**
 * Contract for opening the host app's Room database. The toolkit owns NO schema — the host owns
 * its entities, DAOs and `@Database` class, and applies the Room + KSP Gradle plugins in that
 * module (`docs/HOST_INTEGRATION.md` §1; `demo/shared`'s `DemoDatabase`/`demoNotesModule` is the
 * worked example). This seam supplies only what a host cannot write in `commonMain`: where the
 * file lives on each platform, the platform entry point into [RoomDatabase.Builder] (Android's
 * needs a `Context`, iOS's does not — see [roomDatabaseBuilder]), the toolkit's driver defaults,
 * and the pre-open [SchemaUpgrade] reconciliation.
 *
 * Reach it through the reified [open] extension (`factory.open<MyDb>("my.db")`) or the
 * `databaseSingle` Koin helper. The member overload takes the builder as a lambda so a fake
 * factory in a test never has to produce a real [RoomDatabase].
 */
interface DatabaseFactory {
    /**
     * Opens the database file [name] under the platform's database directory and builds the
     * host's [T] over it.
     *
     * [upgrade] is reconciled first, before the file is touched (see [SchemaUpgrade]). [builder]
     * then receives the resolved file location and returns the host's [RoomDatabase.Builder];
     * the factory applies the toolkit defaults — the bundled SQLite driver and a background query
     * context — runs [configure] over them so a host can override either, and calls `build()`.
     */
    fun <T : RoomDatabase> open(
        name: String,
        upgrade: SchemaUpgrade = SchemaUpgrade.None,
        builder: (location: String) -> RoomDatabase.Builder<T>,
        configure: RoomDatabase.Builder<T>.() -> Unit = {}
    ): T
}

/**
 * [DatabaseFactory.open] with the platform builder supplied for you: `factory.open<MyDb>("my.db")`.
 * [T] must be the host's `@Database` class (annotated `@ConstructedBy`, so Room finds its generated
 * implementation on every platform).
 */
inline fun <reified T : RoomDatabase> DatabaseFactory.open(
    name: String,
    upgrade: SchemaUpgrade = SchemaUpgrade.None,
    noinline configure: RoomDatabase.Builder<T>.() -> Unit = {}
): T = open(name, upgrade, { location -> roomDatabaseBuilder<T>(location) }, configure)