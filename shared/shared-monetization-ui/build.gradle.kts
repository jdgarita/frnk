plugins {
    id("frnk.kmp.library")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.ui"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            // Monetization UI = the design system (atoms/scaffolds/nav) + the monetization domain.
            api(projects.sharedUiAtoms)
            api(projects.sharedMonetizationApi)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
