package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SchemaUpgrade
import dev.jdgarita.frnk.database.SqlDriverFactory
import org.koin.dsl.module

/**
 * Platform SQL driver wiring. The toolkit owns NO schema (restructure Stage 4 / OQ-2): a host
 * defines its own SQLDelight database and builds it through the bound [SqlDriverFactory]
 * (`factory.create(MyDb.Schema, "my.db")` — see `docs/HOST_INTEGRATION.md` §1). The demo's
 * `DemoDB` + `demoNotesModule` (`demo/shared`) is the worked example.
 *
 * The factory resolves the host's [KeyValueStore] **leniently** (`getOrNull`): it is needed only for
 * [SchemaUpgrade.WipeOnVersionBump] (to persist the schema generation per database name), so a host
 * using plain [SchemaUpgrade.None] needn't install `prefsModule`. A host that does request
 * wipe-on-version-bump without a `KeyValueStore` in the graph gets a clear error at `create(...)`.
 */
val databaseModule =
    module {
        single<SqlDriverFactory> { defaultSqlDriverFactory(versionStore = getOrNull<KeyValueStore>()) }
    }