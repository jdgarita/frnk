package dev.jdgarita.frnk.domain.framework

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Base class for all interactors
 * @param dispatcher The dispatcher to use for the coroutine scope
 */
open class BaseInteractor(dispatcher: CoroutineDispatcher) {
    protected val context: CoroutineContext = CoroutineScope(dispatcher + SupervisorJob()).coroutineContext
}