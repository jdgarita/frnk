import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.demo.shared"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
    }

    val xcf = XCFramework("DemoKit")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { t ->
        t.binaries.framework {
            baseName = "DemoKit"
            xcf.add(this)
            isStatic = true
            // Only api-only toolkit modules are exported. The demo deliberately does NOT
            // depend on :shared (which would drag in shared-backend-firebase /
            // shared-monetization-revenuecat / shared-database-impl and their native
            // cinterops). DemoKit.xcframework therefore has no Firebase / RevenueCat /
            // SQLite symbols and the iosDemoApp links and launches without any CocoaPods.
            export(projects.sharedUtils)
            export(projects.sharedUiApi)
            export(projects.sharedUiAtoms)
            export(projects.sharedBackendApi)
            export(projects.sharedMonetizationApi)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
            api(projects.sharedUiApi)
            api(projects.sharedUiAtoms)
            api(projects.sharedBackendApi)
            api(projects.sharedMonetizationApi)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)
        }
    }
}
