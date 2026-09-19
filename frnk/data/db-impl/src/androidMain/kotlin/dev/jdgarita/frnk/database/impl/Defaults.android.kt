package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.di.DatabaseContext

internal actual fun dbPlatform(): DbPlatform =
    object : DbPlatform {
        private val context get() = DatabaseContext.application

        // context.getDatabasePath is Room's own default location, spelled out so the wipe path and
        // the build path resolve the same file by construction.
        override fun databaseLocation(name: String): String = context.getDatabasePath(name).absolutePath

        override fun databaseFileExists(name: String): Boolean = context.getDatabasePath(name).exists()

        override fun deleteDatabaseFiles(name: String) {
            listOf(name, "$name-wal", "$name-shm").forEach { context.getDatabasePath(it).delete() }
        }
    }