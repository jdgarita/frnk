plugins {
    id("frnk.kmp.library")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.bottomnav"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUiAtoms)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)

            // The platform-adaptive bottom nav: Calf renders a native UIKit UITabBar on iOS and a
            // Material3 NavigationBar on Android. This is the SOLE place the toolkit takes Material3 —
            // a deliberate, host-approved trade for true-native iOS chrome with one component. Material3
            // is therefore part of FrnkKit's surface for every consumer of :shared. Calf is pure
            // Kotlin/Compose (no extra native cinterop), so the XCFramework still links under the
            // consumer's existing dynamic_lookup.
            implementation(libs.calf.ui)
            implementation(compose.material3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
