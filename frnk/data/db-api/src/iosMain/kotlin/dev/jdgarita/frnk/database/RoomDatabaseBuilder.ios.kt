package dev.jdgarita.frnk.database

import androidx.room.Room
import androidx.room.RoomDatabase

actual inline fun <reified T : RoomDatabase> roomDatabaseBuilder(location: String): RoomDatabase.Builder<T> =
    Room.databaseBuilder<T>(name = location)