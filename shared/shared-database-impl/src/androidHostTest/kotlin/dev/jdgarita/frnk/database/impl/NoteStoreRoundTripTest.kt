package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.jdgarita.frnk.database.sql.FrnkDB
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the REAL SQLDelight path for [FrnkDB] (BACKLOG P1-1) end-to-end against an in-memory
 * JDBC SQLite driver — the demo binds a fake [dev.jdgarita.frnk.database.NoteStore], so this test
 * is the proof that the generated schema, the `Note.sq` queries, and [SqlDelightNoteStore]'s
 * row→domain mapping actually round-trip. JVM host test: the android/native drivers can't run here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteStoreRoundTripTest {
    private fun newStore(scheduler: TestCoroutineScheduler): SqlDelightNoteStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FrnkDB.Schema.create(driver)
        return SqlDelightNoteStore(FrnkDB(driver), UnconfinedTestDispatcher(scheduler))
    }

    @Test
    fun insert_then_query_round_trips_newest_first() =
        runTest {
            val store = newStore(testScheduler)

            val first = store.add("first note")
            val second = store.add("second note")

            assertTrue(first is AppResult.Success)
            assertTrue(second is AppResult.Success)
            assertEquals("first note", first.data.content)

            val all = store.all()
            assertTrue(all is AppResult.Success)
            assertEquals(listOf("second note", "first note"), all.data.map { it.content })
            // The stored rows carry the id assigned by the DB and a real timestamp.
            assertTrue(all.data.all { it.id > 0L })
            assertTrue(all.data.all { it.createdAt.toEpochMilliseconds() > 0L })
        }

    @Test
    fun clear_removes_all_notes() =
        runTest {
            val store = newStore(testScheduler)
            store.add("doomed")

            val cleared = store.clear()
            val all = store.all()

            assertTrue(cleared is AppResult.Success)
            assertTrue(all is AppResult.Success)
            assertEquals(emptyList(), all.data)
        }
}
