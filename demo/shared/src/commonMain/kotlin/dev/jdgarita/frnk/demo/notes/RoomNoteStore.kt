package dev.jdgarita.frnk.demo.notes

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.datetime.Instant
import kotlin.time.Clock

/**
 * Room-backed [NoteStore] — the real relational path for the demo-owned [DemoDatabase].
 *
 * Room's suspend DAO calls throw on failure and already run on the database's query context (the
 * toolkit's `DatabaseFactory` sets it), so each call is only wrapped and mapped to [AppResult] to
 * keep the never-throw `*-api` contract.
 */
internal class RoomNoteStore(
    private val database: DemoDatabase
) : NoteStore {
    private val dao get() = database.noteDao()

    override suspend fun add(content: String): AppResult<Note, CommonError> =
        runCatchingDb {
            val createdAt = Clock.System.now()
            val id = dao.insert(NoteEntity(content = content, createdAt = createdAt.toEpochMilliseconds()))
            Note(id = id, content = content, createdAt = createdAt)
        }

    override suspend fun all(): AppResult<List<Note>, CommonError> =
        runCatchingDb {
            dao.selectAll().map(::toNote)
        }

    override suspend fun clear(): AppResult<Unit, CommonError> =
        runCatchingDb {
            dao.deleteAll()
        }

    private fun toNote(row: NoteEntity): Note =
        Note(id = row.id, content = row.content, createdAt = Instant.fromEpochMilliseconds(row.createdAt))

    private suspend inline fun <T> runCatchingDb(crossinline block: suspend () -> T): AppResult<T, CommonError> =
        try {
            AppResult.Success(block())
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception
        ) {
            AppResult.Failure(CommonError.Unknown)
        }
}