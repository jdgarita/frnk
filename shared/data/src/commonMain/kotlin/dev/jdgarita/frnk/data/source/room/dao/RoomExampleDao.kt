package dev.jdgarita.frnk.data.source.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.jdgarita.frnk.data.source.room.model.RoomExampleModel
import dev.jdgarita.frnk.data.source.room.model.RoomExamplePartialModel
import dev.jdgarita.frnk.data.source.room.model.RoomExampleWithDetails
import dev.jdgarita.frnk.data.source.room.model.RoomOtherClassModel
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
@Dao
interface RoomExampleDao {

    @Transaction
    @Query("SELECT * FROM RoomExamplePartialModel")
    suspend fun getAll(): List<RoomExampleWithDetails>

    @Transaction
    @Query("SELECT * FROM RoomExamplePartialModel")
    fun getAllAsFlow(): Flow<List<RoomExampleWithDetails>>

    @Transaction
    @Query("SELECT * FROM RoomExamplePartialModel WHERE id = :id")
    suspend fun getById(id: String): RoomExampleWithDetails?

    @Transaction
    suspend fun save(model: RoomExampleModel, otherClassId: String): RoomExampleWithDetails {
        val partialModel = RoomExamplePartialModel(
            id = model.id,
            name = model.name,
            otherClassId = otherClassId,
        )

        savePartial(model = partialModel)

        return getById(id = partialModel.id)!!
    }

    suspend fun create(otherClassId: String, name: String): RoomExampleWithDetails {
        val model = createPartial(otherClassId = otherClassId, name = name)
        return getById(id = model.id)!!
    }

    @Query("DELETE FROM RoomExamplePartialModel WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM RoomExamplePartialModel")
    suspend fun clear()

    fun createFullModel(partial: RoomExamplePartialModel, otherClass: RoomOtherClassModel): RoomExampleModel =
        RoomExampleModel(
            id = partial.id,
            name = partial.name,
            otherClass = otherClass,
        )

    // region Partial

    @Query("SELECT * FROM RoomExamplePartialModel")
    suspend fun getAllPartials(): List<RoomExamplePartialModel>

    @Query("SELECT * FROM RoomExamplePartialModel")
    fun getAllPartialsAsFlow(): Flow<List<RoomExamplePartialModel>>

    @Query("SELECT * FROM RoomExamplePartialModel WHERE id = :id")
    suspend fun getPartialById(id: String): RoomExamplePartialModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: RoomExamplePartialModel)

    @Transaction
    suspend fun savePartial(model: RoomExamplePartialModel): RoomExamplePartialModel {
        // Check if the model exists
        val existingModel = getPartialById(id = model.id)

        // If the model exists and is identical, return it
        if (existingModel == model) {
            return existingModel
        }

        // If different or does not exist, upsert and return the new model
        upsert(model)
        return model
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createPartial(otherClassId: String, name: String): RoomExamplePartialModel {
        val model = RoomExamplePartialModel(
            id = Uuid.random().toString(),
            name = name,
            otherClassId = otherClassId,
        )

        upsert(model)

        return model
    }

    // endregion Partial

}
