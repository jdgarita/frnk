package dev.jdgarita.frnk.database.ext

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.db.SqlSchema
import dev.jdgarita.frnk.database.SqlDriverFactory
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Verifies [databaseSingle] registers a resolvable `single<T>` that builds the database from the
 * Koin-provided [SqlDriverFactory], forwarding the host's [SqlSchema] + name unchanged. A recording
 * factory captures the arguments; the driver is a never-invoked no-op fixture since the helper only
 * threads it into the host's constructor lambda.
 */
class DatabaseSingleTest {
    private class FakeDb(
        val driver: SqlDriver
    )

    @Test
    fun registers_single_and_forwards_schema_and_name() {
        var recordedSchema: SqlSchema<QueryResult.Value<Unit>>? = null
        var recordedName: String? = null
        val factory =
            SqlDriverFactory { schema, name ->
                recordedSchema = schema
                recordedName = name
                NoopSqlDriver
            }

        val app =
            koinApplication {
                modules(
                    module {
                        single<SqlDriverFactory> { factory }
                        databaseSingle(FakeSchema, "fake.db") { driver -> FakeDb(driver) }
                    }
                )
            }
        try {
            val db = app.koin.get<FakeDb>()

            assertSame(NoopSqlDriver, db.driver, "driver from the factory is threaded into the constructor")
            assertSame(FakeSchema, recordedSchema, "host schema forwarded to the factory unchanged")
            assertEquals("fake.db", recordedName, "database name forwarded to the factory unchanged")
        } finally {
            app.close()
        }
    }

    private companion object {
        val FakeSchema =
            object : SqlSchema<QueryResult.Value<Unit>> {
                override val version: Long = 1

                override fun create(driver: SqlDriver): QueryResult.Value<Unit> = QueryResult.Unit

                override fun migrate(
                    driver: SqlDriver,
                    oldVersion: Long,
                    newVersion: Long,
                    vararg callbacks: AfterVersion
                ): QueryResult.Value<Unit> = QueryResult.Unit
            }

        /** Satisfies the [SqlDriver] contract; never invoked — [databaseSingle] only passes it through. */
        val NoopSqlDriver =
            object : SqlDriver {
                override fun <R> executeQuery(
                    identifier: Int?,
                    sql: String,
                    mapper: (SqlCursor) -> QueryResult<R>,
                    parameters: Int,
                    binders: (SqlPreparedStatement.() -> Unit)?
                ): QueryResult<R> = throw UnsupportedOperationException()

                override fun execute(
                    identifier: Int?,
                    sql: String,
                    parameters: Int,
                    binders: (SqlPreparedStatement.() -> Unit)?
                ): QueryResult<Long> = throw UnsupportedOperationException()

                override fun newTransaction(): QueryResult<Transacter.Transaction> = throw UnsupportedOperationException()

                override fun currentTransaction(): Transacter.Transaction? = null

                override fun addListener(
                    vararg queryKeys: String,
                    listener: Query.Listener
                ) = Unit

                override fun removeListener(
                    vararg queryKeys: String,
                    listener: Query.Listener
                ) = Unit

                override fun notifyListeners(vararg queryKeys: String) = Unit

                override fun close() = Unit
            }
    }
}