package com.tweener.kmpship.data._internal.libs.room

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tweener.kmpship.data.source.room.MyProjectRoomDatabase
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

    private lateinit var database: MyProjectRoomDatabase

    fun init() {
        database = databaseHelper.createDatabase()
        Napier.d { "Room database initialized." }
    }

    fun getDatabase(): MyProjectRoomDatabase = database
}
