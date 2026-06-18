package dev.jdgarita.frnk.demo.notes

import dev.jdgarita.frnk.demo.sql.DemoDB
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlin.time.Clock

/**
 * SQLDelight-backed [NoteStore] — the real relational path for the demo-owned [DemoDB].
 *
 * SQLDelight queries throw on failure, so each call is wrapped and mapped to [AppResult] to keep
 * the never-throw `*-api` contract. Work runs on [dispatcher] (default [Dispatchers.Default]) so
 * the synchronous driver never blocks the caller's thread; tests inject a test dispatcher.
 */
internal class SqlDelightNoteStore(
    private val db: DemoDB,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : NoteStore {
    private val queries get() = db.noteQueries

    override suspend fun add(content: String): AppResult<Note, CommonError> =
        runCatchingDb {
            val createdAt = Clock.System.now()
            queries.insert(content = content, createdAt = createdAt.toEpochMilliseconds())
            val id = queries.lastInsertedId().executeAsOne()
            Note(id = id, content = content, createdAt = createdAt)
        }

    override suspend fun all(): AppResult<List<Note>, CommonError> =
        runCatchingDb {
            queries.selectAll(::toNote).executeAsList()
        }

    override suspend fun clear(): AppResult<Unit, CommonError> =
        runCatchingDb {
            queries.deleteAll()
        }

    private fun toNote(
        id: Long,
        content: String,
        createdAt: Long
    ): Note = Note(id = id, content = content, createdAt = Instant.fromEpochMilliseconds(createdAt))

    private suspend inline fun <T> runCatchingDb(crossinline block: () -> T): AppResult<T, CommonError> =
        withContext(dispatcher) {
            try {
                AppResult.Success(block())
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception
            ) {
                AppResult.Failure(CommonError.Unknown)
            }
        }
}