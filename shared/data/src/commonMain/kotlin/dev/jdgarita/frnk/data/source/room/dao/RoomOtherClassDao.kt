package dev.jdgarita.frnk.data.source.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.jdgarita.frnk.data.source.room.model.RoomOtherClassModel
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
@Dao
interface RoomOtherClassDao {

    @Query("SELECT * FROM RoomOtherClassModel")
    suspend fun getAll(): List<RoomOtherClassModel>

    @Query("SELECT * FROM RoomOtherClassModel")
    fun getAllAsFlow(): Flow<List<RoomOtherClassModel>>

    @Query("SELECT * FROM RoomOtherClassModel WHERE id = :id")
    suspend fun getById(id: String): RoomOtherClassModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: RoomOtherClassModel)

    @Transaction
    suspend fun save(model: RoomOtherClassModel): RoomOtherClassModel {
        // Check if the model exists
        val existingModel = getById(id = model.id)

        // If the model exists and is identical, return it
        if (existingModel == model) {
            return existingModel
        }

        // If different or does not exist, upsert and return the new model
        upsert(model)
        return model
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun create(name: String): RoomOtherClassModel {
        val model = RoomOtherClassModel(
            id = Uuid.random().toString(),
            name = name,
        )

        upsert(model)

        return model
    }

    @Query("DELETE FROM RoomOtherClassModel WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM RoomOtherClassModel")
    suspend fun clear()

}
