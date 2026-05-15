import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    val xcf = XCFramework(ProjectConfiguration.IOS_FRAMEWORK_NAME)
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { t ->
        t.binaries.framework {
            baseName = ProjectConfiguration.IOS_FRAMEWORK_NAME
            xcf.add(this)
            isStatic = true
            export(projects.shared)
            // Bundled impls (RevenueCat, Firebase) reference native iOS frameworks
            // (PurchasesHybridCommon, etc.) that the host app provides via CocoaPods / SPM.
            // Defer symbol resolution so the toolkit's XCFramework links without those
            // native deps present.
            linkerOpts("-undefined", "dynamic_lookup")
        }
    }
    sourceSets.iosMain.dependencies {
        api(projects.shared)
    }
}
