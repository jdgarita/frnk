package dev.jdgarita.frnk.data._internal.libs.room

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.jdgarita.frnk.data.source.room.FrnkRoomDatabase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
class RoomConfiguration(
    private val databaseHelper: RoomDatabaseHelper,
) {

    private lateinit var database: FrnkRoomDatabase

    fun init() {
        database = databaseHelper.createDatabase()
        Napier.d { "Room database initialized." }
    }

    fun getDatabase(): FrnkRoomDatabase = database
}
