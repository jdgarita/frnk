package com.tweener.kmpship.data.source.room.datasource

import com.tweener.kmpship.data.source.room.dao.RoomExampleDao
import com.tweener.kmpship.data.source.room.dao.RoomOtherClassDao
import com.tweener.kmpship.data.source.room.model.RoomExampleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
class RoomExampleDataSource(
    private val roomExampleDao: RoomExampleDao,
    private val roomOtherClassDao: RoomOtherClassDao,
) {

    suspend fun getAll(): List<RoomExampleModel> =
        roomExampleDao
            .getAll()
            .map { roomExampleDao.createFullModel(partial = it.example, otherClass = it.otherClass) }

    fun getAllAsFlow(): Flow<List<RoomExampleModel>> =
        roomExampleDao
            .getAllAsFlow()
            .map { models ->
                models.map { roomExampleDao.createFullModel(partial = it.example, otherClass = it.otherClass) }
            }

    suspend fun getById(id: String): RoomExampleModel? =
        roomExampleDao
            .getById(id = id)?.let { roomExampleDao.createFullModel(partial = it.example, otherClass = it.otherClass) }

    suspend fun save(model: RoomExampleModel): RoomExampleModel {
        val otherClass = roomOtherClassDao.save(model = model.otherClass)
        val savedModel = roomExampleDao.save(model = model, otherClassId = otherClass.id)

        return roomExampleDao.createFullModel(partial = savedModel.example, otherClass = savedModel.otherClass)
    }

    suspend fun create(otherClassId: String, name: String): RoomExampleModel {
        val model = roomExampleDao.create(otherClassId = otherClassId, name = name)
        return roomExampleDao.createFullModel(partial = model.example, otherClass = model.otherClass)
    }

    suspend fun delete(id: String) {
        roomExampleDao.deleteById(id = id)
    }
}
