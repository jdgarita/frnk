package dev.jdgarita.frnk.database

import dev.jdgarita.frnk.common.AppError
import dev.jdgarita.frnk.common.AppResult

interface KeyValueStore {
    suspend fun put(key: String, value: String): AppResult<Unit, StorageError>
    suspend fun get(key: String): AppResult<String?, StorageError>
    suspend fun remove(key: String): AppResult<Unit, StorageError>
}

sealed class StorageError(override val message: String) : AppError {
    data class Io(val cause: String) : StorageError("IO: $cause")
}
