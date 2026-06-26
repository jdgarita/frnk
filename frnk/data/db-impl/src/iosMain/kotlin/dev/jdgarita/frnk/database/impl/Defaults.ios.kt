package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual fun dbPlatform(): DbPlatform =
    object : DbPlatform {
        private val fileManager get() = NSFileManager.defaultManager

        override fun createDriver(
            schema: SqlSchema<QueryResult.Value<Unit>>,
            name: String
        ): SqlDriver =
            NativeSqliteDriver(
                schema = schema,
                name = name,
                onConfiguration = { config ->
                    config.copy(extendedConfig = config.extendedConfig.copy(basePath = databasesDir()))
                }
            )

        override fun databaseFileExists(name: String): Boolean = fileManager.fileExistsAtPath("${databasesDir()}/$name")

        override fun deleteDatabaseFiles(name: String) {
            val dir = databasesDir()
            listOf(name, "$name-wal", "$name-shm").forEach { fileName ->
                fileManager.removeItemAtPath("$dir/$fileName", error = null)
            }
        }

        // Single base dir for the DB file — injected into NativeSqliteDriver (create path) AND used by
        // exists/delete (wipe path), so delete-path == create-path by construction (closes m2). Mirrors
        // SQLiter's own default (NSApplicationSupportDirectory/databases) so an existing on-disk DB is still
        // found; created here because passing an explicit basePath bypasses SQLiter's auto-create.
        private fun databasesDir(): String {
            val appSupport =
                NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
                    .first() as String
            val dir = "$appSupport/databases"
            if (!fileManager.fileExistsAtPath(dir)) {
                fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
            }
            return dir
        }
    }