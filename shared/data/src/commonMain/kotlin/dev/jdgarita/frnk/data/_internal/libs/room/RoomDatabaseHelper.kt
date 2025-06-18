package dev.jdgarita.frnk.data._internal.libs.room

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.jdgarita.frnk.data.source.room.FrnkRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
abstract class RoomDatabaseHelper {

    companion object {
        const val DATABASE_FILENAME = "myproject.db"
    }

    /**
     * Creates a new database.
     */
    fun createDatabase(): FrnkRoomDatabase =
        createDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    /**
     * Deletes the actual database.
     */
    abstract fun deleteDatabase()

    /**
     * Deletes the actual database and creates a new one.
     */
    fun resetDatabase() {
        deleteDatabase()
        createDatabase()
    }

    protected abstract fun createDatabaseBuilder(): RoomDatabase.Builder<FrnkRoomDatabase>

    protected fun getDatabaseFilename(): String = DATABASE_FILENAME
}
