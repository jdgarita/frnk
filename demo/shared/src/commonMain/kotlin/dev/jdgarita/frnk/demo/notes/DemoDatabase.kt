package dev.jdgarita.frnk.demo.notes

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * The demo-owned Room database (restructure Stage 4 / OQ-2): the worked example of a host schema
 * opened through the toolkit's `DatabaseFactory` (`demoNotesModule`). The exported schema lives
 * under `demo/shared/schemas/` the way a host's should, so a version bump is reviewable.
 */
@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
@ConstructedBy(DemoDatabaseConstructor::class)
internal abstract class DemoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

// Room's KSP processor generates the actual on every target from the @ConstructedBy annotation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object DemoDatabaseConstructor : RoomDatabaseConstructor<DemoDatabase> {
    override fun initialize(): DemoDatabase
}