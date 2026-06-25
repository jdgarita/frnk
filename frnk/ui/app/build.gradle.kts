plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.app"
    }
    sourceSets {
        commonMain.dependencies {
            // Apex of the ui column: FrnkAppShell + the adaptive bar (Material3 arrives transitively
            // from here — the accepted batteries-included trade), the paywall view layer, and the
            // observability contracts. NO *-impl compile deps — EntitlementManager/AnalyticsTracker
            // are resolved from Koin at runtime; hosts install the impl modules they choose.
            api(projects.uiBottomNav)
            api(projects.sharedMonetizationUi)
            api(projects.analyticsApi)
            // Cinterop-clean api edge (interfaces only — Firebase lives in :remote-config-impl) so
            // frnkModules { } can default remoteConfig to noopRemoteConfigModule and
            // validateFrnkBootstrap can resolve RemoteConfigService. Mirrors the :analytics-api edge.
            api(projects.remoteConfigApi)
            // The bootstrap FrnkAppScaffold's fail-fast assertion points hosts at.
            api(projects.coreDi)
        }
    }
}