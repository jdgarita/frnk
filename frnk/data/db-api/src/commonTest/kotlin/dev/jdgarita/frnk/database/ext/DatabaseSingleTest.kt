package dev.jdgarita.frnk.database.ext

import androidx.room.RoomDatabase
import dev.jdgarita.frnk.database.DatabaseFactory
import dev.jdgarita.frnk.database.SchemaUpgrade
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies [databaseSingle] registers a resolvable `single<T>` that opens the database through the
 * Koin-provided [DatabaseFactory], forwarding the host's name + upgrade unchanged. A recording
 * factory captures the arguments and stops there — it never runs the builder lambda, so no
 * platform `Context` or generated Room implementation is needed here — and Koin surfaces that
 * stop as a failed resolution, which is what the test unwinds.
 */
class DatabaseSingleTest {
    private abstract class FakeDb : RoomDatabase()

    private class FactoryReached : RuntimeException("stopped at the factory")

    private class RecordingDatabaseFactory : DatabaseFactory {
        var recordedName: String? = null
        var recordedUpgrade: SchemaUpgrade? = null
        var builderInvoked = false

        override fun <T : RoomDatabase> open(
            name: String,
            upgrade: SchemaUpgrade,
            builder: (location: String) -> RoomDatabase.Builder<T>,
            configure: RoomDatabase.Builder<T>.() -> Unit
        ): T {
            recordedName = name
            recordedUpgrade = upgrade
            throw FactoryReached()
        }
    }

    @Test
    fun registers_single_and_forwards_name_and_upgrade() {
        val factory = RecordingDatabaseFactory()
        val app =
            koinApplication {
                modules(
                    module {
                        single<DatabaseFactory> { factory }
                        databaseSingle<FakeDb>("fake.db", SchemaUpgrade.WipeOnVersionBump(3))
                    }
                )
            }
        try {
            val failure = assertFails { app.koin.get<FakeDb>() }

            assertTrue(
                generateSequence(failure) { it.cause }.any { it is FactoryReached },
                "resolution reached the factory: $failure"
            )
            assertEquals("fake.db", factory.recordedName, "database name forwarded to the factory unchanged")
            assertEquals(SchemaUpgrade.WipeOnVersionBump(3), factory.recordedUpgrade, "upgrade forwarded unchanged")
            assertFalse(factory.builderInvoked, "the builder is the factory's to run, not the helper's")
        } finally {
            app.close()
        }
    }

    @Test
    fun defaults_to_no_schema_upgrade() {
        val factory = RecordingDatabaseFactory()
        val app =
            koinApplication {
                modules(
                    module {
                        single<DatabaseFactory> { factory }
                        databaseSingle<FakeDb>("fake.db")
                    }
                )
            }
        try {
            assertFails { app.koin.get<FakeDb>() }

            assertEquals(SchemaUpgrade.None, factory.recordedUpgrade)
        } finally {
            app.close()
        }
    }
}