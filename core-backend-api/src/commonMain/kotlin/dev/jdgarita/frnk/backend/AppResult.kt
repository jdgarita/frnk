package dev.jdgarita.frnk.backend

/** Toolkit-wide result envelope. Every backend interface returns this; callers exhaustive-when on it. */
sealed interface AppResult<out D, out E : AppError> {
    data class Success<D>(
        val data: D,
    ) : AppResult<D, Nothing>

    data class Failure<E : AppError>(
        val error: E,
    ) : AppResult<Nothing, E>
}

interface AppError {
    val message: String
}

enum class CommonError(
    override val message: String,
) : AppError {
    Network("Network unavailable"),
    Unauthorized("Authentication required"),
    NotFound("Resource not found"),
    Unknown("Unknown error"),
}
