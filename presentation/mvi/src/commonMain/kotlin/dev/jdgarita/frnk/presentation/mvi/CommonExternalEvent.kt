package dev.jdgarita.frnk.presentation.mvi

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Common [ExternalEvent]s that is handled by all [NavigationRouter]s.
 */
@JsExport
sealed class CommonExternalEvent : ExternalEvent {
    @Serializable
    @SerialName("DidPressBack")
    data class DidPressBack(val screenId: String? = null) : CommonExternalEvent()
}