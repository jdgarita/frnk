package dev.jdgarita.frnk.domain.framework.outcome

/**
 * Outcome class encapsulates the result from a request.
 *
 * Methods returning an Outcome will never throw an exception.  Instead, they would return
 * an Outcome.Error.
 */
sealed interface Outcome<out TResult, out TError> {
    data class Success<out TResult>(val data: TResult) : Outcome<TResult, Nothing>

    data class Error<out TError>(val error: TError) : Outcome<Nothing, TError>

    fun asSuccess(): TResult? =
        (this as? Success)?.data

    fun asError(): TError? =
        (this as? Error)?.error

    suspend fun doOnSuccess(onSuccess: suspend (data: TResult) -> Unit): Outcome<TResult, TError> =
        this.also {
            when (this) {
                is Success -> onSuccess(data)
                is Error -> {}
            }
        }

    suspend fun doOnError(onError: suspend (error: TError) -> Unit): Outcome<TResult, TError> =
        this.also {
            when (this) {
                is Success -> {}
                is Error -> onError(error)
            }
        }

    fun <TMapped> mapSuccess(map: (data: TResult) -> TMapped): Outcome<TMapped, TError> =
        when (this) {
            is Success -> Success(map(data))
            is Error -> this
        }

    fun <TMapped> mapError(mapError: (error: TError) -> TMapped): Outcome<TResult, TMapped> =
        when (this) {
            is Success -> this
            is Error -> Error(mapError(error))
        }

    suspend fun <RMapped, EMapped> flatMap(
        mapSuccess: suspend (data: TResult) -> Outcome<RMapped, EMapped>,
        mapError: suspend (error: TError) -> Outcome<RMapped, EMapped>
    ): Outcome<RMapped, EMapped> =
        when (this) {
            is Success -> mapSuccess(data)
            is Error -> mapError(error)
        }

    fun <RMapped, EMapped> map(
        mapSuccess: (data: TResult) -> Outcome<RMapped, EMapped>,
        mapError: (error: TError) -> Outcome<RMapped, EMapped>
    ): Outcome<RMapped, EMapped> =
        when (this) {
            is Success -> mapSuccess(data)
            is Error -> mapError(error)
        }
}

fun Outcome<Any, Any>.ignoreOutcome(): Outcome<Unit, Unit> =
    when (this) {
        is Outcome.Error -> Outcome.Error(Unit)
        is Outcome.Success -> Outcome.Success(Unit)
    }

suspend fun <TResult, TError, TMapped> Outcome<TResult, TError>.flatMapSuccess(
    mapSuccess: suspend (data: TResult) -> Outcome<TMapped, TError>
): Outcome<TMapped, TError> =
    when (this) {
        is Outcome.Success -> mapSuccess(data)
        is Outcome.Error -> Outcome.Error(error)
    }

suspend fun <TResult, TError, TMapped> Outcome<TResult, TError>.flatMapError(
    mapError: suspend (error: TError) -> Outcome<TResult, TMapped>
): Outcome<TResult, TMapped> =
    when (this) {
        is Outcome.Success -> this
        is Outcome.Error -> mapError(error)
    }

fun <T : Any> T.toOutcomeError(): Outcome<Nothing, T> =
    Outcome.Error(this)

fun <T : Any> T.toOutcomeSuccess(): Outcome<T, Nothing> =
    Outcome.Success(this)
