package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun dbPlatform(): DbPlatform =
    object : DbPlatform {
        private val fileManager get() = NSFileManager.defaultManager

        override fun createDriver(
            schema: SqlSchema<QueryResult.Value<Unit>>,
            name: String
        ): SqlDriver = NativeSqliteDriver(schema, name)

        override fun databaseFileExists(name: String): Boolean {
            val path = documentsDir()?.URLByAppendingPathComponent(name)?.path ?: return false
            return fileManager.fileExistsAtPath(path)
        }

        override fun deleteDatabaseFiles(name: String) {
            val dir = documentsDir() ?: return
            listOf(name, "$name-wal", "$name-shm").forEach { fileName ->
                val url = dir.URLByAppendingPathComponent(fileName) ?: return@forEach
                fileManager.removeItemAtURL(url, error = null)
            }
        }

        // NOTE (m2, deferred): NativeSqliteDriver may store the DB outside Documents, so this wipe can
        // be a no-op on iOS. Faithful port of still's existing behavior; fix by passing an explicit
        // DatabaseConfiguration path to NativeSqliteDriver and deleting that same path.
        private fun documentsDir(): NSURL? =
            fileManager
                .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
                .firstOrNull() as? NSURL
    }