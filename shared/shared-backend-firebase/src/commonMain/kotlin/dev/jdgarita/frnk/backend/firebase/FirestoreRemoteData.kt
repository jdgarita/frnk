package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.RemoteData
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

internal class FirestoreRemoteData : RemoteData {
    override suspend fun <T : Any> get(
        collection: String,
        id: String,
        decode: (Map<String, Any?>) -> T,
    ): AppResult<T, CommonError> = TODO("wire dev.gitlive:firebase-firestore")

    override suspend fun <T : Any> set(
        collection: String,
        id: String,
        value: T,
        encode: (T) -> Map<String, Any?>,
    ): AppResult<Unit, CommonError> = TODO("wire dev.gitlive:firebase-firestore")
}
