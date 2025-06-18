package com.tweener.kmpship.data.source.firebase.remoteconfig.model

/**
 * Enum class with all the keys defined in Firebase Remote Config.
 *
 * [https://console.firebase.google.com/u/0/project/{yourProjectId}/config/env/firebase](https://console.firebase.google.com/u/0/project/{yourProjectId}/config/env/firebase)
 *
 * @author Vivien Mahe
 * @since 29/12/2023
 */
enum class RemoteConfigKey(val value: String) {
    EXAMPLE_FEATURE_FLAG("example_feature_flag"),
    APP_RATING_ASK_PERIOD_MONTHS("app_rating_ask_period_months"),
    // TODO Add here the Firebase remote config properties, ie:
}
