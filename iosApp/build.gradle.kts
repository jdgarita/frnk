import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

/**
 * Builds a fat XCFramework so Swift Package Manager can vend the library.
 * Run `./gradlew :iosApp:assembleXCFramework` to produce
 * `iosApp/build/XCFrameworks/release/FrnkKit.xcframework`.
 *
 * Declared before the kotlin { } block so each iOS target can register its
 * framework into the same fat XCFramework.
 */
val xcf = XCFramework("FrnkKit")

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "FrnkKit"
            isStatic = true
            export(project(":core-common"))
            export(project(":core-network-api"))
            export(project(":core-database-api"))
            export(project(":core-ui-atoms"))
            xcf.add(this)
        }
    }

    sourceSets {
        iosMain.dependencies {
            api(project(":core-common"))
            api(project(":core-network-api"))
            api(project(":core-network-impl"))
            api(project(":core-database-api"))
            api(project(":core-database-impl"))
            api(project(":core-ui-atoms"))
        }
    }
}
