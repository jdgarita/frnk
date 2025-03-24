package dev.jdgarita.frnk.presentation.componentCore

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a view state for a component.
 */
interface ViewState {
    /**
     * The unique identifier for this view state. It is necessary so that the component can be
     * identified in the view state container. Used for example for click listeners.
     *
     * Usually the id should be the same as the id of the model backing the view state.
     */
    val id: String

    companion object {
        /**
         * The default identifier for a view state. This is provided for convenience when
         * creating view states that do not require a unique identifier. Many times, child
         * view states within composite organisms might not need individual identifiers,
         * as the parent organism's ID serves as the primary identifier of interest.
         * For instance, when an organism is composed of multiple child view states,
         * those child view states will often share the same ID as the parent organism.
         * This constant can be used as a placeholder in such cases.
         */
        const val DEFAULT_ID = ""

        @OptIn(ExperimentalUuidApi::class)
        val UNIQUE_ID get() = "${Uuid.random()}"
    }
}