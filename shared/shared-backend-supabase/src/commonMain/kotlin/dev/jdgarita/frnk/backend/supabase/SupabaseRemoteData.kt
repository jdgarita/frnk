package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AppResult
import dev.jdgarita.frnk.backend.CommonError
import dev.jdgarita.frnk.backend.RemoteData

internal class SupabaseRemoteData : RemoteData {
    override suspend fun <T : Any> get(
        collection: String,
        id: String,
        decode: (Map<String, Any?>) -> T,
    ): AppResult<T, CommonError> = TODO("wire supabase-postgrest-kt")

    override suspend fun <T : Any> set(
        collection: String,
        id: String,
        value: T,
        encode: (T) -> Map<String, Any?>,
    ): AppResult<Unit, CommonError> = TODO("wire supabase-postgrest-kt")
}
