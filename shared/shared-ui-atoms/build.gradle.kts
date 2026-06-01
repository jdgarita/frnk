plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.atoms"
        compileSdk = ProjectConfiguration.COMPILE_SDK
        minSdk = ProjectConfiguration.MIN_SDK
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared_ui_atoms" } }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUiApi)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            // Lifecycle-aware Compose collection (collectAsStateWithLifecycle / repeatOnLifecycle)
            // powering FrnkMviScreen + EffectCollector. api so hosts inherit it for their own screens.
            api(libs.androidx.lifecycle.runtime.compose)

            api(libs.compose.unstyled.theming)
            implementation(libs.compose.unstyled.primitives)
            implementation(libs.compose.unstyled.platformtheme)
            implementation(libs.compose.unstyled.button)
            implementation(libs.compose.unstyled.icon)
            implementation(libs.compose.unstyled.separators)
            implementation(libs.compose.ripple.indication)
            implementation(libs.icons.lucide)
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
    }
}
