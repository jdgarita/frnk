package dev.jdgarita.frnk.domain.framework

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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

suspend fun <T1, T2, E> zipOutcomesTogether(
    t1func: suspend () -> Outcome<T1, E>,
    t2func: suspend () -> Outcome<T2, E>
): Outcome<Pair<T1, T2>, E> = zipOutcomes(t1func, t2func) { t1, t2 ->
    t1 to t2
}

suspend fun <T1, T2, R, E> zipOutcomes(
    t1func: suspend () -> Outcome<T1, E>,
    t2func: suspend () -> Outcome<T2, E>,
    mapper: (T1, T2) -> R
): Outcome<R, E> = coroutineScope {
    val t1 = async { t1func() }
    val t2 = async { t2func() }

    val t1Result = t1.await()
    val t2Result = t2.await()

    if (t1Result is Outcome.Success && t2Result is Outcome.Success) {
        Outcome.Success(mapper(t1Result.data, t2Result.data))
    } else {
        val firstError = (t1Result as? Outcome.Error) ?: (t2Result as Outcome.Error)
        firstError
    }
}

suspend fun <T1, T2, T3, E> zipOutcomesTogether(
    t1func: suspend () -> Outcome<T1, E>,
    t2func: suspend () -> Outcome<T2, E>,
    t3func: suspend () -> Outcome<T3, E>
): Outcome<Triple<T1, T2, T3>, E> = zipOutcomes(t1func, t2func, t3func) { t1, t2, t3 ->
    Triple(t1, t2, t3)
}

suspend fun <T1, T2, T3, R, E> zipOutcomes(
    t1func: suspend () -> Outcome<T1, E>,
    t2func: suspend () -> Outcome<T2, E>,
    t3func: suspend () -> Outcome<T3, E>,
    mapper: (T1, T2, T3) -> R
): Outcome<R, E> = coroutineScope {
    val t1 = async { t1func() }
    val t2 = async { t2func() }
    val t3 = async { t3func() }

    val t1Result = t1.await()
    val t2Result = t2.await()
    val t3Result = t3.await()

    if (t1Result is Outcome.Success && t2Result is Outcome.Success && t3Result is Outcome.Success) {
        Outcome.Success(mapper(t1Result.data, t2Result.data, t3Result.data))
    } else {
        val firstError = (t1Result as? Outcome.Error) ?: (t2Result as? Outcome.Error) ?: (t3Result as Outcome.Error)
        firstError
    }
}

suspend fun <T1, T2, T3, T4, R, E> zipOutcomes(
    t1func: suspend () -> Outcome<T1, E>,
    t2func: suspend () -> Outcome<T2, E>,
    t3func: suspend () -> Outcome<T3, E>,
    t4func: suspend () -> Outcome<T4, E>,
    mapper: (T1, T2, T3, T4) -> R
): Outcome<R, E> = coroutineScope {
    val t1 = async { t1func() }
    val t2 = async { t2func() }
    val t3 = async { t3func() }
    val t4 = async { t4func() }

    val t1Result = t1.await()
    val t2Result = t2.await()
    val t3Result = t3.await()
    val t4Result = t4.await()

    if (
        t1Result is Outcome.Success &&
        t2Result is Outcome.Success &&
        t3Result is Outcome.Success &&
        t4Result is Outcome.Success
    ) {
        Outcome.Success(mapper(t1Result.data, t2Result.data, t3Result.data, t4Result.data))
    } else {
        val firstError = (t1Result as? Outcome.Error)
            ?: (t2Result as? Outcome.Error)
            ?: (t3Result as? Outcome.Error)
            ?: (t4Result as Outcome.Error)
        firstError
    }
}

suspend fun <T1, T2, T3, T4, T5, R, E> zipOutcomes(
    t1func: suspend () -> Outcome<T1, E>,
    t2func: suspend () -> Outcome<T2, E>,
    t3func: suspend () -> Outcome<T3, E>,
    t4func: suspend () -> Outcome<T4, E>,
    t5func: suspend () -> Outcome<T5, E>,
    mapper: (T1, T2, T3, T4, T5) -> R
): Outcome<R, E> = coroutineScope {
    val t1 = async { t1func() }
    val t2 = async { t2func() }
    val t3 = async { t3func() }
    val t4 = async { t4func() }
    val t5 = async { t5func() }

    val t1Result = t1.await()
    val t2Result = t2.await()
    val t3Result = t3.await()
    val t4Result = t4.await()
    val t5Result = t5.await()

    if (
        t1Result is Outcome.Success &&
        t2Result is Outcome.Success &&
        t3Result is Outcome.Success &&
        t4Result is Outcome.Success &&
        t5Result is Outcome.Success
    ) {
        Outcome.Success(mapper(t1Result.data, t2Result.data, t3Result.data, t4Result.data, t5Result.data))
    } else {
        val firstError = (t1Result as? Outcome.Error)
            ?: (t2Result as? Outcome.Error)
            ?: (t3Result as? Outcome.Error)
            ?: (t4Result as? Outcome.Error)
            ?: (t5Result as Outcome.Error)
        firstError
    }
}