package dev.jdgarita.frnk.data._internal.libs.room

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.jdgarita.frnk.data.source.room.FrnkRoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
@OptIn(ExperimentalForeignApi::class)
class RoomIosDatabaseHelper : RoomDatabaseHelper() {

    override fun createDatabaseBuilder(): RoomDatabase.Builder<FrnkRoomDatabase> = Room.databaseBuilder<FrnkRoomDatabase>(name = documentDirectory() + "/" + getDatabaseFilename())

    override fun deleteDatabase() {
        NSFileManager.defaultManager.removeItemAtPath(path = documentDirectory() + getDatabaseFilename(), error = null)
    }

    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}
