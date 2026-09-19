package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.DatabaseFactory
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SchemaUpgrade
import org.koin.dsl.module

/**
 * Platform Room wiring. The toolkit owns NO schema: a host defines its own `@Database` (entities,
 * DAOs, the Room + KSP plugins in its own module) and opens it through the bound [DatabaseFactory]
 * (`databaseSingle<MyDb>("my.db")` — see `docs/HOST_INTEGRATION.md` §1). The demo's
 * `DemoDatabase` + `demoNotesModule` (`demo/shared`) is the worked example.
 *
 * The factory resolves the host's [KeyValueStore] **leniently** (`getOrNull`): it is needed only for
 * [SchemaUpgrade.WipeOnVersionBump] (to persist the schema generation per database name), so a host
 * using plain [SchemaUpgrade.None] needn't install `prefsModule`. A host that does request
 * wipe-on-version-bump without a `KeyValueStore` in the graph gets a clear error at `open(...)`.
 */
val databaseModule =
    module {
        single<DatabaseFactory> { defaultDatabaseFactory(versionStore = getOrNull<KeyValueStore>()) }
    }