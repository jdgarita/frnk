plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.prefs.api"
    }
    // Pure stdlib: KeyValueStore + the typed Preference<T> layer have no third-party deps.
}
