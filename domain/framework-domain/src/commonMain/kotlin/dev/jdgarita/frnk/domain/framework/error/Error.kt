package dev.jdgarita.frnk.domain.framework.error

/**
 * Represents a data error that can occur in the application.
 */
sealed class Error {
    data object Network : Error()
    data object Server : Error()
    data object Unauthorized : Error()
    data object NotFound : Error()

    /**
     * Represents an error that occurred while making a request.
     * If needed can be mapped to a more specific error based on the [errorCode] and [httpStatusCode].
     *
     * @property httpStatusCode The HTTP status code of the response.
     * @property errorCode The error code of the response.
     */
    data class Request(
        val httpStatusCode: Int? = null,
        val errorCode: String? = null,
        val body: String? = null
    ) : Error()

    /**
     * Represents an error that occurred while fetching data from the cache.
     */
    data object Cache : Error()
    data class Unknown(val cause: Throwable? = null) : Error()

    fun parseErrorForAnalytics(): String =
        when (val error = this@Error) {
            Cache -> "Client cache"
            Network -> "Client network"
            is Request -> "Request; http status code: ${error.httpStatusCode} - error code: ${error.errorCode}"
            Server -> "Server request"
            Unauthorized -> "Unauthorized"
            NotFound -> "Not Found"
            is Unknown -> "Unknown - reason: ${error.cause?.message}"
        }
}