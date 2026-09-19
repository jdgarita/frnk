package dev.jdgarita.frnk.database.impl

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun dbPlatform(): DbPlatform =
    object : DbPlatform {
        private val fileManager get() = NSFileManager.defaultManager

        override fun databaseLocation(name: String): String = "${databasesDir()}/$name"

        override fun databaseFileExists(name: String): Boolean = fileManager.fileExistsAtPath(databaseLocation(name))

        override fun deleteDatabaseFiles(name: String) {
            listOf(name, "$name-wal", "$name-shm").forEach { fileName ->
                fileManager.removeItemAtPath(databaseLocation(fileName), error = null)
            }
        }

        // One base dir for the file — used by the build path AND by exists/delete (the wipe path),
        // so delete-path == create-path by construction. Application Support itself, no `databases/`
        // subfolder: that is where the blueprint host (Faint) has kept its Room file since launch,
        // so a host moving onto this seam finds its existing data. Backed up by iCloud/iTunes like
        // the rest of Application Support; created on demand because URLForDirectory only creates
        // the directory it names.
        private fun databasesDir(): String {
            val directory =
                checkNotNull(
                    fileManager.URLForDirectory(
                        directory = NSApplicationSupportDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = true,
                        error = null
                    )
                ) { "Application Support directory unavailable" }
            return checkNotNull(directory.path) { "Application Support directory has no path" }
        }
    }