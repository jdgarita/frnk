package dev.jdgarita.frnk.database

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.jdgarita.frnk.di.DatabaseContext

actual inline fun <reified T : RoomDatabase> roomDatabaseBuilder(location: String): RoomDatabase.Builder<T> =
    Room.databaseBuilder<T>(context = DatabaseContext.application, name = location)