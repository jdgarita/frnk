package dev.jdgarita.frnk.demo.notes

import dev.jdgarita.frnk.database.SqlDriverFactory
import dev.jdgarita.frnk.demo.sql.DemoDB
import org.koin.dsl.module

/**
 * Real persistence wiring for the demo's Notes section: builds the demo-owned [DemoDB] through
 * the toolkit's [SqlDriverFactory] — exactly the way a real host injects its own schema
 * (restructure Stage 4 / OQ-2) — and binds [SqlDelightNoteStore] over it.
 *
 * Requires a [SqlDriverFactory] in the graph, so install `databaseModule` (`:data-db-impl`)
 * alongside. `androidDemoApp` overrides the default in-memory `FakeNoteStore` with this;
 * DemoKit/iOS keeps the fake so the framework stays free of the SQLite driver.
 */
val demoNotesModule =
    module {
        single { DemoDB(get<SqlDriverFactory>().create(DemoDB.Schema, DEMO_DATABASE_FILE_NAME)) }
        single<NoteStore> { SqlDelightNoteStore(get()) }
    }

private const val DEMO_DATABASE_FILE_NAME = "demo.db"
