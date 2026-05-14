package dev.jdgarita.frnk.network.internal

import dev.jdgarita.frnk.common.AppResult
import dev.jdgarita.frnk.network.NetworkClient
import dev.jdgarita.frnk.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

internal class KtorNetworkClient(
    private val client: HttpClient,
) : NetworkClient {
    override suspend fun <T> get(
        path: String,
        deserializer: (String) -> T,
    ): AppResult<T, NetworkError> =
        runCatching {
            val response = client.get(path)
            if (response.status.isSuccess()) {
                AppResult.Success(deserializer(response.bodyAsText()))
            } else {
                AppResult.Failure(NetworkError.Http(response.status.value, response.bodyAsText()))
            }
        }.getOrElse { AppResult.Failure(NetworkError.Unknown(it.message ?: "unknown")) }

    override suspend fun <T> post(
        path: String,
        body: String,
        deserializer: (String) -> T,
    ): AppResult<T, NetworkError> =
        runCatching {
            val response = client.post(path) { setBody(body) }
            if (response.status.isSuccess()) {
                AppResult.Success(deserializer(response.bodyAsText()))
            } else {
                AppResult.Failure(NetworkError.Http(response.status.value, response.bodyAsText()))
            }
        }.getOrElse { AppResult.Failure(NetworkError.Unknown(it.message ?: "unknown")) }
}
