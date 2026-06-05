plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.bottomnav"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUiAtoms)

            // The platform-adaptive bottom nav: Calf renders a native UIKit UITabBar on iOS and a
            // Material3 NavigationBar on Android. This is the SOLE place the toolkit takes Material3 —
            // a deliberate, host-approved trade for true-native iOS chrome with one component. Material3
            // is therefore part of FrnkKit's surface for every consumer of :shared. Calf is pure
            // Kotlin/Compose (no extra native cinterop), so the XCFramework still links under the
            // consumer's existing dynamic_lookup.
            implementation(libs.calf.ui)
            implementation(compose.material3)
        }
    }
}
