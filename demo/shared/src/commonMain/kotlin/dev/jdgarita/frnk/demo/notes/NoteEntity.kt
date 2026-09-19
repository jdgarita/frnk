package dev.jdgarita.frnk.demo.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The demo-owned `note` row (restructure Stage 4 / OQ-2) — demo scaffolding showing the Room path a
 * real host wires through `:data-db-api`'s `DatabaseFactory`. The entity stays in this package;
 * [RoomNoteStore] maps it to the demo's [Note] domain model so no Room type leaks out.
 */
@Entity(tableName = "note")
internal data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val content: String,
    val createdAt: Long
)