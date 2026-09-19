package dev.jdgarita.frnk.demo.notes

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the REAL Room path for the demo-owned [DemoDatabase] end-to-end against an in-memory
 * database — proof that the generated schema, the [NoteDao] queries, and [RoomNoteStore]'s
 * row→domain mapping actually round-trip. Robolectric host test: Room's Android builder needs a
 * `Context`, and the framework driver is the one that runs on the JVM (the bundled driver the
 * toolkit's `DatabaseFactory` installs ships Android/iOS binaries; on a device `demoNotesModule` +
 * `databaseModule` provide that real path instead).
 */
@RunWith(RobolectricTestRunner::class)
class NoteStoreRoundTripTest {
    private val database =
        Room
            .inMemoryDatabaseBuilder<DemoDatabase>(RuntimeEnvironment.getApplication())
            .setDriver(AndroidSQLiteDriver())
            .build()
    private val store = RoomNoteStore(database)

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insert_then_query_round_trips_newest_first() =
        runTest {
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
            store.add("doomed")

            val cleared = store.clear()
            val all = store.all()

            assertTrue(cleared is AppResult.Success)
            assertTrue(all is AppResult.Success)
            assertEquals(emptyList(), all.data)
        }
}