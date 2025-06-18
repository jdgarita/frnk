package com.tweener.kmpship.data._internal.libs.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tweener.kmpship.data.source.room.MyProjectRoomDatabase

/**
 * @author Vivien Mahe
 * @since 19/01/2025
 */
class RoomAndroidDatabaseHelper(private val context: Context) : RoomDatabaseHelper() {

    override fun createDatabaseBuilder(): RoomDatabase.Builder<MyProjectRoomDatabase> =
        Room.databaseBuilder<MyProjectRoomDatabase>(context = context, name = context.getDatabasePath(getDatabaseFilename()).absolutePath)


    override fun deleteDatabase() {
        context.deleteDatabase(getDatabaseFilename())
    }
}
