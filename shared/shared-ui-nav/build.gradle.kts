plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

// Bundled toolkit nav icons (frnk_nav_home/settings/add) for the adaptive-nav-bar engine, which takes
// DrawableResource icons (not ImageVector). Pin the generated accessor's package so call sites can import
// `dev.jdgarita.frnk.ui.bottomnav.generated.resources.Res` deterministically.
compose.resources {
    publicResClass = true
    packageOfResClass = "${ProjectConfiguration.GROUP_ID}.ui.bottomnav.generated.resources"
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.bottomnav"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUiAtoms)

            // The platform-adaptive bottom nav. This is the SOLE place the toolkit takes Material3 —
            // a deliberate, host-approved trade. Material3 is therefore part of FrnkKit's surface for
            // every consumer of :shared. Both bar engines are pure Kotlin/Compose (no extra native
            // cinterop), so the XCFramework still links under the consumer's existing dynamic_lookup.
            //
            // Two engines coexist for the POC (FrnkAdaptiveNavEngine, selectable at runtime), so we can
            // A/B their UX/performance directly:
            //  - Calf — native UIKit UITabBar on iOS, Material3 NavigationBar on Android (the default).
            //  - adaptive-nav-bar — Material3 NavigationBar on Android, a native glassy UITabBar (iOS 26+)
            //    / Material3 bar (older) on iOS, plus a built-in "add" button (FAB on Android, inline on
            //    iOS). Its icons are resource-based (DrawableResource + SF-Symbol string), so this module
            //    also pulls in compose.components.resources and bundles the toolkit's default nav icons.
            implementation(libs.calf.ui)
            implementation(libs.adaptive.nav.bar)
            implementation(compose.material3)
            implementation(compose.components.resources)
        }
    }
}
