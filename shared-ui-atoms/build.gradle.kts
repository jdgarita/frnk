plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_ui_atoms" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUiApi)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            implementation(libs.compose.unstyled.core)
            implementation(libs.compose.unstyled.theming)
            implementation(libs.compose.unstyled.platformtheme)
            implementation(libs.composables.core)
        }
    }
}

android {
    namespace = "${ProjectConfiguration.GROUP_ID}.ui.atoms"
    compileSdk = ProjectConfiguration.COMPILE_SDK
    defaultConfig { minSdk = ProjectConfiguration.MIN_SDK }
}
