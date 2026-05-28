plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.database.impl"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_database_impl" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedDatabaseApi)
            implementation(libs.koin.core)
            implementation(libs.settings.core)
            implementation(libs.settings.coroutines)
        }
        androidMain.dependencies { implementation(libs.sqldelight.android.driver) }
        iosMain.dependencies { implementation(libs.sqldelight.native.driver) }
    }
}
