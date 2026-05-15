plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.shared"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(projects.sharedUiApi)
            api(projects.sharedUiAtoms)
            api(projects.sharedDatabaseApi)
            api(projects.sharedBackendApi)
            api(projects.sharedMonetizationApi)

            api(projects.sharedDatabaseImpl)
            api(projects.sharedBackendFirebase)
            api(projects.sharedBackendSupabase)
            api(projects.sharedMonetizationRevenuecat)

            implementation(libs.koin.core)
        }
    }
}
