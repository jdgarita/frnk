package dev.jdgarita.frnk.data.source.room.datasource

import dev.jdgarita.frnk.data.source.room.dao.RoomOtherClassDao
import dev.jdgarita.frnk.data.source.room.model.RoomOtherClassModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
class RoomOtherClassDataSource(
    private val roomOtherClassDao: RoomOtherClassDao,
) {

    suspend fun get(): RoomOtherClassModel? =
        roomOtherClassDao
            .getAll()
            .firstOrNull()

    fun getAsFlow(): Flow<RoomOtherClassModel?> =
        roomOtherClassDao
            .getAllAsFlow()
            .map { it.firstOrNull() }

    suspend fun save(model: RoomOtherClassModel): RoomOtherClassModel =
        roomOtherClassDao.save(model = model)

    suspend fun create(name: String): RoomOtherClassModel =
        roomOtherClassDao.create(name = name)
}
