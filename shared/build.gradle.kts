plugins {
    id("frnk.kmp.base")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.shared"
        withHostTest {}
    }
    // Bare iOS targets — NO framework: :shared is aggregated into FrnkKit by :iosApp (which exports it),
    // so it must not declare its own framework binary. That's why :shared uses frnk.kmp.base, not
    // frnk.kmp.library (which would add a framework).
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(projects.sharedUiApi)
            api(projects.sharedUiAtoms)
            // Platform-adaptive bottom navigation (Calf-backed: native UITabBar on iOS, Material3
            // NavigationBar on Android). This is the ONE module that intentionally pulls in Material3,
            // so it ships in FrnkKit for every consumer — a deliberate, host-approved trade.
            api(projects.sharedUiNav)
            api(projects.sharedDatabaseApi)
            api(projects.sharedBackendApi)
            api(projects.sharedMonetizationApi)
            api(projects.sharedMonetizationUi)

            api(projects.sharedDatabaseImpl)
            api(projects.sharedBackendFirebase)
            api(projects.sharedBackendSupabase)
            api(projects.sharedMonetizationRevenuecat)

            implementation(libs.koin.core)
        }
    }
}
