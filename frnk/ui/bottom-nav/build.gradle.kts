plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

// Bundled toolkit nav icons (frnk_nav_home/settings/add) for the adaptive-nav-bar engine, which takes
// DrawableResource icons (not ImageVector). Pin the generated accessor's package so call sites can import
// `dev.jdgarita.frnk.ui.bottomnav.generated.resources.Res` deterministically.
compose.resources {
    publicResClass = true
    packageOfResClass = "${libs.versions.frnk.groupId.get()}.ui.bottomnav.generated.resources"
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.bottomnav"
    }
    sourceSets {
        commonMain.dependencies {
            // The design system + Compose MVI/Nav bindings (transitively :ui-components → :ui-theme →
            // :haptics, plus the core-mvi/core-nav contracts). Re-pointed off the deleted :shared-ui-atoms
            // facade at restructure Stage 9; :ui-scaffolds covers every ui.{atoms,theme,mvi,nav,scaffolds}
            // import this module uses (incl. LocalFrnkBottomBarInset).
            api(projects.uiScaffolds)

            // The platform-adaptive bottom bar (narendraanjana09's adaptive-nav-bar): a Material3
            // NavigationBar on Android, a native glassy UITabBar (iOS 26+) / Material3 bar (older) on iOS,
            // plus a built-in "add" button (FAB on Android, inline on iOS). This is the SOLE place the
            // toolkit takes Material3 — a deliberate, host-approved trade that reaches every consumer of
            // this module (and of :ui-app above it). The library is pure Kotlin/Compose (no extra native
            // cinterop), so umbrella XCFrameworks still link under the consumer's existing dynamic_lookup.
            //
            // Its icons are resource-based (DrawableResource + SF-Symbol string), so this module also
            // pulls in compose.components.resources and bundles the toolkit's default nav icons. NOTE:
            // under AGP 9.2.1's com.android.kotlin.multiplatform.library + Compose MP 1.11.1, those
            // DrawableResources do NOT package into the Android APK from this KMP library module — every
            // Android host must ship them as raw assets (see docs/HOST_INTEGRATION.md and the demo's
            // demo/android-app/src/main/assets/composeResources/).
            implementation(libs.adaptive.nav.bar)
            implementation(compose.material3)
            implementation(compose.components.resources)
        }
    }
}
