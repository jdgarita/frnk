package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.jdgarita.frnk.di.DatabaseContext

internal actual fun dbPlatform(): DbPlatform =
    object : DbPlatform {
        private val context get() = DatabaseContext.application

        override fun createDriver(
            schema: SqlSchema<QueryResult.Value<Unit>>,
            name: String
        ): SqlDriver = AndroidSqliteDriver(schema, context, name)

        override fun databaseFileExists(name: String): Boolean = context.getDatabasePath(name).exists()

        override fun deleteDatabaseFiles(name: String) {
            listOf(name, "$name-wal", "$name-shm").forEach { context.getDatabasePath(it).delete() }
        }
    }