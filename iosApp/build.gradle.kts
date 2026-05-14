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
            export(projects.coreUtils)
            export(projects.coreUiApi)
            export(projects.coreUiAtoms)
            export(projects.coreDatabaseApi)
            export(projects.coreBackendApi)
            export(projects.coreMonetizationApi)
        }
    }
    sourceSets.iosMain.dependencies {
        api(projects.coreUtils)
        api(projects.coreUiApi)
        api(projects.coreUiAtoms)
        api(projects.coreDatabaseApi)
        api(projects.coreBackendApi)
        api(projects.coreMonetizationApi)
    }
}
