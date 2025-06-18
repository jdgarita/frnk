package dev.jdgarita.frnk.data._internal.libs.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.jdgarita.frnk.data.source.room.FrnkRoomDatabase

/**
 * @author Vivien Mahe
 * @since 19/01/2025
 */
class RoomAndroidDatabaseHelper(private val context: Context) : RoomDatabaseHelper() {

    override fun createDatabaseBuilder(): RoomDatabase.Builder<FrnkRoomDatabase> =
        Room.databaseBuilder<FrnkRoomDatabase>(context = context, name = context.getDatabasePath(getDatabaseFilename()).absolutePath)


    override fun deleteDatabase() {
        context.deleteDatabase(getDatabaseFilename())
    }
}
