package com.tweener.kmpship.data.source.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.tweener.kmpship.data.source.room.dao.RoomExampleDao
import com.tweener.kmpship.data.source.room.dao.RoomOtherClassDao
import com.tweener.kmpship.data.source.room.model.RoomExamplePartialModel
import com.tweener.kmpship.data.source.room.model.RoomOtherClassModel

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
abstract class MyProjectRoomDatabase : RoomDatabase() {

    abstract fun getExampleDao(): RoomExampleDao

    abstract fun getOtherClassDao(): RoomOtherClassDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<MyProjectRoomDatabase> {
    override fun initialize(): MyProjectRoomDatabase
}
