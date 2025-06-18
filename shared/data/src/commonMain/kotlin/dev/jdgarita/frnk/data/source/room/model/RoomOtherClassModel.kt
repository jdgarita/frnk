package dev.jdgarita.frnk.data.source.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Vivien Mahe
 * @since 10/01/2025
 */
@Entity
data class RoomOtherClassModel(
    @PrimaryKey val id: String,
    val name: String,
)
