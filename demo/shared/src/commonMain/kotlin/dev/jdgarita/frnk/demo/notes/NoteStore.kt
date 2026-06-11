package dev.jdgarita.frnk.demo.notes

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.datetime.Instant

/**
 * A persisted note — demo scaffolding (restructure Stage 4 / OQ-2) demonstrating the end-to-end
 * SQLDelight path a real host wires: a demo-owned schema ([dev.jdgarita.frnk.demo.sql.DemoDB])
 * built through the toolkit's `SqlDriverFactory` (`:data-db-api`). This is the domain model: the
 * generated `DemoDB` row type never crosses this boundary.
 */
data class Note(
    val id: Long,
    val content: String,
    val createdAt: Instant,
)

/**
 * Typed access over the `note` table. Follows the toolkit's `*-api` convention — returns
 * [AppResult] and never throws, so callers handle failures with an exhaustive `when`. The real
 * SQLDelight binding is [SqlDelightNoteStore] (`demoNotesModule`); `demoModule` binds the
 * in-memory `FakeNoteStore` by default so DemoKit stays free of the SQLite driver.
 */
interface NoteStore {
    /** Persists a new note with the given [content] and returns the stored row (with its id + timestamp). */
    suspend fun add(content: String): AppResult<Note, CommonError>

    /** All notes, newest first. */
    suspend fun all(): AppResult<List<Note>, CommonError>

    /** Removes every note. */
    suspend fun clear(): AppResult<Unit, CommonError>
}
