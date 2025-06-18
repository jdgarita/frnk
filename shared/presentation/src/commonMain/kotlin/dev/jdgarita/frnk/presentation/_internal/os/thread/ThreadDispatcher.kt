package dev.jdgarita.frnk.presentation._internal.os.thread

/**
 * @author Vivien Mahe
 * @since 07/09/2023
 */
interface ThreadDispatcher<T> {

    fun io(): T

    fun ui(): T
}
