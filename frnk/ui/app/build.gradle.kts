plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.app"
    }
    sourceSets {
        commonMain.dependencies {
            // Apex of the ui column: FrnkAppShell + the adaptive bar (Material3 arrives transitively
            // from here — the accepted batteries-included trade), the paywall view layer, and the
            // observability contracts. NO *-impl compile deps — EntitlementManager/AnalyticsTracker
            // are resolved from Koin at runtime; hosts install the impl modules they choose.
            api(projects.sharedUiNav)
            api(projects.sharedMonetizationUi)
            api(projects.analyticsApi)
            // The bootstrap FrnkAppScaffold's fail-fast assertion points hosts at.
            api(projects.coreDi)
        }
    }
}
