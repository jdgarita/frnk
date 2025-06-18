package dev.jdgarita.frnk.data.source.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import dev.jdgarita.frnk.data.source.room.dao.RoomExampleDao
import dev.jdgarita.frnk.data.source.room.dao.RoomOtherClassDao
import dev.jdgarita.frnk.data.source.room.model.RoomExamplePartialModel
import dev.jdgarita.frnk.data.source.room.model.RoomOtherClassModel

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
@Database(
    entities = [
        RoomExamplePartialModel::class,
        RoomOtherClassModel::class,
    ],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class FrnkRoomDatabase : RoomDatabase() {

    abstract fun getExampleDao(): RoomExampleDao

    abstract fun getOtherClassDao(): RoomOtherClassDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<FrnkRoomDatabase> {
    override fun initialize(): FrnkRoomDatabase
}
