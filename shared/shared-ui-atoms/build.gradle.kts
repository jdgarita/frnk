plugins {
    id("frnk.kmp.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

// NB: this module does NOT use frnk.kmp.library.hosttest — its tests live in an `androidHostTest`
// source set (Compose UI tests under Robolectric), and it needs the `isIncludeAndroidResources = true`
// withHostTest variant below, not the plain withHostTest + commonTest deps the hosttest plugin adds.

kotlin {
    // Apply the default source-set hierarchy explicitly. This module adds a custom `commonDebug`
    // intermediate (cross-platform @Preview code) via manual `dependsOn` edges below; those edges
    // otherwise make KGP skip auto-applying the default template and emit a "template not applied"
    // warning. Calling it here applies the template (the standard commonMain → android/ios… graph)
    // and leaves the commonDebug edges as additive parents on top.
    applyDefaultHierarchyTemplate()
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.atoms"
        // P4-4: design-system tests run as JVM host tests (testAndroidHostTest) under Robolectric.
        // Robolectric needs the merged Android resources/manifest to inflate the Compose test host.
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUiApi)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            // Lifecycle-aware Compose collection (collectAsStateWithLifecycle / repeatOnLifecycle)
            // powering FrnkMviScreen + EffectCollector. api so hosts inherit it for their own screens.
            api(libs.androidx.lifecycle.runtime.compose)
            // Toolkit navigation primitives (FrnkNavHost / frnkComposable / rememberFrnkNavigator)
            // wrap JetBrains CMP navigation-compose. api so hosts can reference NavController types.
            // Pure Kotlin/Compose — no native cinterop, so DemoKit.xcframework stays clean.
            api(libs.androidx.navigation)

            api(libs.compose.unstyled.theming)
            implementation(libs.compose.unstyled.primitives)
            implementation(libs.compose.unstyled.platformtheme)
            implementation(libs.compose.unstyled.button)
            implementation(libs.compose.unstyled.icon)
            implementation(libs.compose.unstyled.separators)
            implementation(libs.compose.ripple.indication)
            implementation(libs.icons.lucide)
            // Cross-platform haptics behind LocalFrnkHaptics (installed by FrnkTheme). Like the
            // ripple, this is a UI-feedback library, not a swappable backend SDK. multihaptic ships
            // its own Android/iOS impls (no native cinterop), so DemoKit/FrnkKit XCFrameworks stay
            // clean and the Android VIBRATE permission self-merges from multihaptic-core's manifest.
            implementation(libs.multihaptic.core)
            implementation(libs.multihaptic.compose)
        }

        // commonDebug: cross-platform source set for @Preview composables.
        // Sits between commonMain and each platform source set so previews compile
        // for Android + iOS. The AGP-9 KMP-Android library plugin has a single
        // androidMain compilation (no compileDebug/Release split), so previews
        // also ship in release AARs today — they're inert @Composable functions
        // and R8 strips them. Move to a sibling module if true debug-only
        // exclusion becomes load-bearing.
        val commonDebug by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.compose.ui.tooling.preview)
            }
        }
        val androidMain by getting {
            dependsOn(commonDebug)
            dependencies {
                implementation(libs.compose.ui.tooling)
            }
        }
        val iosArm64Main by getting { dependsOn(commonDebug) }
        val iosSimulatorArm64Main by getting { dependsOn(commonDebug) }

        // P4-4: Compose UI tests for the highest-value atoms. They drive the headless atoms through
        // a real composition (runComposeUiTest) on the JVM host via Robolectric, so they run under
        // `testAndroidHostTest` (what CI gates) with no device/emulator. Android-host-only — the
        // Compose test runtime + Robolectric have no common/iOS variant, so this stays out of
        // commonTest (matches shared-database-impl's androidHostTest-scoped JDBC driver precedent).
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.compose.ui.test)
            // Registers androidx.activity.ComponentActivity in the test manifest so the Compose test
            // host (runComposeUiTest) can launch it under Robolectric.
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.robolectric)
        }
    }
}
