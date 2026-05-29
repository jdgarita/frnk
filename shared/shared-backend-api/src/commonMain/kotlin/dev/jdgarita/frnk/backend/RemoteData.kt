package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

/** Minimal remote read/write surface. Implementations decide what 'collection' means. */
interface RemoteData {
    suspend fun <T : Any> get(
        collection: String,
        id: String,
        decode: (Map<String, Any?>) -> T,
    ): AppResult<T, CommonError>

    suspend fun <T : Any> set(
        collection: String,
        id: String,
        value: T,
        encode: (T) -> Map<String, Any?>,
    ): AppResult<Unit, CommonError>
}
