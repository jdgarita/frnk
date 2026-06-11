package dev.jdgarita.frnk.database

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.datetime.Instant

/**
 * A persisted note — the toolkit's first relational entity (BACKLOG P1-1), demonstrating the
 * end-to-end SQLDelight path. This is the **api** domain model: the generated `FrnkDB` row type
 * stays inside `:shared-database-impl` and never crosses this boundary.
 */
data class Note(
    val id: Long,
    val content: String,
    val createdAt: Instant,
)

/**
 * Typed access over the `note` table. Like every `*-api` interface it returns [AppResult] and
 * never throws, so callers handle failures with an exhaustive `when`. The concrete SQLDelight
 * binding lives in `:shared-database-impl` (`databaseModule`); tests and the demo can fake it.
 */
interface NoteStore {
    /** Persists a new note with the given [content] and returns the stored row (with its id + timestamp). */
    suspend fun add(content: String): AppResult<Note, CommonError>

    /** All notes, newest first. */
    suspend fun all(): AppResult<List<Note>, CommonError>

    /** Removes every note. */
    suspend fun clear(): AppResult<Unit, CommonError>
}
