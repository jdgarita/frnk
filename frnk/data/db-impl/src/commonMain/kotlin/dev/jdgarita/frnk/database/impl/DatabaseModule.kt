package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.SqlDriverFactory
import org.koin.dsl.module

/**
 * Platform SQL driver wiring. The toolkit owns NO schema (restructure Stage 4 / OQ-2): a host
 * defines its own SQLDelight database and builds it through the bound [SqlDriverFactory]
 * (`factory.create(MyDb.Schema, "my.db")` — see `docs/HOST_INTEGRATION.md` §1). The demo's
 * `DemoDB` + `demoNotesModule` (`demo/shared`) is the worked example. Key-value persistence is a
 * separate axis: install `prefsModule` (`:data-prefs-impl`) for the `KeyValueStore` binding.
 */
val databaseModule =
    module {
        single<SqlDriverFactory> { defaultSqlDriverFactory() }
    }