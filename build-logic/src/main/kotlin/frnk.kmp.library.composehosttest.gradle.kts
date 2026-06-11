import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// frnk KMP-library + Compose + design-system-test/preview convention plugin (restructure Stage 7b).
//
// Composes `frnk.kmp.library.compose` with the test + preview wiring the design-system modules
// (:ui-components, :ui-scaffolds) share — extracted from :shared-ui-atoms' hand-written build so the
// two split modules don't each repeat it. Three things, none of which the plain `frnk.kmp.library.hosttest`
// plugin covers:
//
//  1. `withHostTest { isIncludeAndroidResources = true }` — these modules' tests are Compose UI tests
//     driven through a real composition (`runComposeUiTest`) under Robolectric on the JVM host
//     (`testAndroidHostTest` — what CI gates), so Robolectric needs the merged Android resources/manifest
//     to inflate the test host. The base hosttest plugin's plain `withHostTest {}` + commonTest deps
//     don't fit (the Compose UI-test runtime + Robolectric have no common/iOS variant, so the suite lives
//     in `androidHostTest`, not `commonTest`).
//  2. A `commonDebug` intermediate source set for cross-platform `@Preview` code, wired as a `dependsOn`
//     parent of `androidMain` + both iOS source sets so previews compile on every target. AGP 9's
//     single-`androidMain` compilation means `commonDebug` also ships in release AARs today — inert
//     `@Composable`s that R8 strips; promote to a sibling module if true exclusion becomes load-bearing.
//  3. The `androidHostTest` dependency bundle (kotlin-test + coroutines-test + compose ui-test +
//     ui-test-manifest + robolectric).

plugins {
    id("frnk.kmp.library.compose")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    // The custom `commonDebug` intermediate (manual `dependsOn` edges below) otherwise makes KGP skip the
    // default source-set template and warn; applying it explicitly keeps the standard graph + the extra edges.
    applyDefaultHierarchyTemplate()
    android {
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        // commonDebug: cross-platform @Preview source set between commonMain and each platform set.
        val commonDebug by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.findLibrary("compose-ui-tooling-preview").get())
            }
        }
        val androidMain by getting {
            dependsOn(commonDebug)
            dependencies {
                implementation(libs.findLibrary("compose-ui-tooling").get())
            }
        }
        val iosArm64Main by getting { dependsOn(commonDebug) }
        val iosSimulatorArm64Main by getting { dependsOn(commonDebug) }

        getByName("androidHostTest").dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            implementation(libs.findLibrary("compose-ui-test").get())
            // Registers androidx.activity.ComponentActivity in the test manifest so the Compose test
            // host (runComposeUiTest) can launch it under Robolectric.
            implementation(libs.findLibrary("androidx-compose-ui-test-manifest").get())
            implementation(libs.findLibrary("robolectric").get())
        }
    }
}
