package com.tweener.kmpship.presentation

import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Vivien Mahe
 * @since 04/06/2023
 */
object PresentationConstants {

    /**
     * Delay between two emissions of a flow, to avoid flood.
     */
    val FLOWS_DEBOUNCE_DELAY = 300.milliseconds

    object Size {
        object Shape {
            val Small = 4.dp
            val Medium = 8.dp
            val Large = 12.dp
            val ExtraLarge = 16.dp
        }
    }
}
