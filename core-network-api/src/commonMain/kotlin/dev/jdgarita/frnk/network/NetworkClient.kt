package dev.jdgarita.frnk.network

import dev.jdgarita.frnk.common.AppError
import dev.jdgarita.frnk.common.AppResult

/**
 * Pure-interface contract for any HTTP client. Domain code depends only on
 * this; the concrete Ktor (or future replacement) implementation lives in
 * :core-network-impl and is bound via Koin.
 */
interface NetworkClient {
    suspend fun <T> get(
        path: String,
        deserializer: (String) -> T,
    ): AppResult<T, NetworkError>

    suspend fun <T> post(
        path: String,
        body: String,
        deserializer: (String) -> T,
    ): AppResult<T, NetworkError>
}

sealed class NetworkError(
    override val message: String,
) : AppError {
    data class Http(
        val code: Int,
        val body: String?,
    ) : NetworkError("HTTP $code")

    data object NoInternet : NetworkError("No internet connection")

    data class Unknown(
        val cause: String,
    ) : NetworkError("Unknown: $cause")
}
