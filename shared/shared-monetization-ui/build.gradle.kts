plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.ui"
    }
    sourceSets {
        commonMain.dependencies {
            // Monetization UI = the design system (atoms/scaffolds/nav) + the monetization domain.
            api(projects.sharedUiAtoms)
            api(projects.sharedMonetizationApi)
        }
    }
}
