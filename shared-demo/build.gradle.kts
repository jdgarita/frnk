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
            export(projects.shared)
            // :shared bundles RevenueCat + Firebase impls whose cinterop references native
            // iOS frameworks (PurchasesHybridCommon, etc.). iosDemoApp brings those in via
            // CocoaPods; defer resolution so this framework links without them.
            linkerOpts("-undefined", "dynamic_lookup")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)
        }
    }
}
