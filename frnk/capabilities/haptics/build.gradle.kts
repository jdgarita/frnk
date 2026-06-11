plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.haptics"
    }
    sourceSets {
        commonMain.dependencies {
            // Compose- and library-free haptics CONTRACT only (HapticType / HapticFeedback / HapticEngine
            // SPI + Default/NoOp). StateFlow for the enabled flag is the sole dependency. The multihaptic
            // Compose engine binding stays in :shared-ui-atoms until restructure Stage 7a.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
