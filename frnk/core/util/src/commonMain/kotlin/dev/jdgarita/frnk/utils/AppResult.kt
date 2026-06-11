package dev.jdgarita.frnk.utils

/** Toolkit-wide result envelope. Every `*-api` interface returns this; callers exhaustive-when on it. */
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

/**
 * Collapses both arms of an [AppResult] into a single value. The exhaustive `when` over the
 * sealed hierarchy keeps the success/failure contract compile-checked at every call site.
 */
inline fun <D, E : AppError, R> AppResult<D, E>.fold(
    onSuccess: (D) -> R,
    onFailure: (E) -> R,
): R =
    when (this) {
        is AppResult.Success -> onSuccess(data)
        is AppResult.Failure -> onFailure(error)
    }
