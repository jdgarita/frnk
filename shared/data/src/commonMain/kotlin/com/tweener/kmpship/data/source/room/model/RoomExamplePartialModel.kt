package com.tweener.kmpship.data.source.room.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
@Entity
data class RoomExamplePartialModel(
    @PrimaryKey val id: String,
    val name: String,
    val otherClassId: String,
)

data class RoomExampleWithDetails(
    @Embedded val example: RoomExamplePartialModel,

    @Relation(
        parentColumn = "otherClassId",
        entityColumn = "id"
    )
    val otherClass: RoomOtherClassModel,
)

data class RoomExampleModel(
    val id: String,
    val name: String,
    val otherClass: RoomOtherClassModel,
)
