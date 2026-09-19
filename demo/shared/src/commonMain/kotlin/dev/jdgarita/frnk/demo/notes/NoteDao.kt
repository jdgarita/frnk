package dev.jdgarita.frnk.demo.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface NoteDao {
    /** Inserts [note] and returns the row id the database assigned. */
    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("SELECT * FROM note ORDER BY createdAt DESC, id DESC")
    suspend fun selectAll(): List<NoteEntity>

    @Query("DELETE FROM note")
    suspend fun deleteAll()
}