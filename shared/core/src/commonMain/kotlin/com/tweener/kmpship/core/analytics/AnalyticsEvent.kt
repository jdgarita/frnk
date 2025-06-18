package com.tweener.kmpship.core.analytics

/**
 * @author Vivien Mahe
 * @since 18/02/2025
 */
sealed class AnalyticsEvent(val name: String) {
    data object MyEventToTrack : AnalyticsEvent("my_event_to_track")
}
