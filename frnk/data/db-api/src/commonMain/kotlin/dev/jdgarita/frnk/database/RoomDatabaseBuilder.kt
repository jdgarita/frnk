package dev.jdgarita.frnk.database

import androidx.room.RoomDatabase

/**
 * The one line of Room a host cannot write in `commonMain`: `Room.databaseBuilder` takes a
 * `Context` on Android and only a name elsewhere. [location] is the resolved file location a
 * [DatabaseFactory] hands its builder lambda — an absolute path on both platforms.
 *
 * The Android actual reads the `Application` from `:core-di`'s `DatabaseContext`, which the
 * `initializeFrnk(context, …)` overload sets; a host that bypasses it must set the seam itself
 * before the first `open`.
 */
expect inline fun <reified T : RoomDatabase> roomDatabaseBuilder(location: String): RoomDatabase.Builder<T>