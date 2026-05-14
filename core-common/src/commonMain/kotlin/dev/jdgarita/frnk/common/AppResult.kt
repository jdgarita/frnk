package dev.jdgarita.frnk.common

/**
 * Either-style sealed result used across every *-api boundary so callers can
 * exhaustively handle success vs. typed errors without exceptions.
 *
 * Renamed from `Result` to avoid clashing with kotlin.Result in callers.
 */
sealed interface AppResult<out D, out E : AppError> {
    data class Success<out D>(
        val data: D,
    ) : AppResult<D, Nothing>

    data class Failure<out E : AppError>(
        val error: E,
    ) : AppResult<Nothing, E>
}

interface AppError {
    val message: String
}

inline fun <D, E : AppError, R> AppResult<D, E>.fold(
    onSuccess: (D) -> R,
    onFailure: (E) -> R,
): R =
    when (this) {
        is AppResult.Success -> onSuccess(data)
        is AppResult.Failure -> onFailure(error)
    }

inline fun <D, E : AppError, R> AppResult<D, E>.map(transform: (D) -> R): AppResult<R, E> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> this
    }
