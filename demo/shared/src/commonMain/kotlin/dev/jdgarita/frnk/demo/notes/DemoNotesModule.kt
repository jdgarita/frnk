package dev.jdgarita.frnk.demo.notes

import dev.jdgarita.frnk.database.DatabaseFactory
import dev.jdgarita.frnk.database.ext.databaseSingle
import org.koin.dsl.module

/**
 * Real persistence wiring for the demo's Notes section: opens the demo-owned [DemoDatabase]
 * through the toolkit's [DatabaseFactory] — exactly the way a real host opens its own schema
 * (restructure Stage 4 / OQ-2) — and binds [RoomNoteStore] over it.
 *
 * Requires a [DatabaseFactory] in the graph, so install `databaseModule` (`:data-db-impl`)
 * alongside. `androidDemoApp` overrides the default in-memory `FakeNoteStore` with this;
 * DemoKit/iOS keeps the fake so the framework stays free of the bundled SQLite driver.
 */
val demoNotesModule =
    module {
        databaseSingle<DemoDatabase>(DEMO_DATABASE_FILE_NAME)
        single<NoteStore> { RoomNoteStore(get()) }
    }

private const val DEMO_DATABASE_FILE_NAME = "demo.db"