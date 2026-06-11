plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.prefs.api"
    }
    // Pure stdlib: KeyValueStore + the typed Preference<T> layer have no third-party deps.
}
