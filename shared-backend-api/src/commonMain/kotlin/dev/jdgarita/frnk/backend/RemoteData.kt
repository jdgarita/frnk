package dev.jdgarita.frnk.backend

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
