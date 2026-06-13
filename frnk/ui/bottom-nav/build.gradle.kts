plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

// A single bundled placeholder drawable, referenced ONLY by the iOS actual to satisfy adaptive-nav-bar's
// non-null `NavigationItem.icon`/`IosFabItem.icon` (`DrawableResource`) — the visible iOS 26+ glass icon
// comes from the SF-Symbol `systemIcon`; the drawable only surfaces on the older-iOS Compose fallback. It
// lives in commonMain/composeResources because the Compose-resources plugin generates the `Res` accessor
// into commonMain regardless; Android code never reads it, so no host asset-packaging step is needed. Pin
// the accessor's package so the call site imports `…ui.bottomnav.generated.resources.Res` deterministically.
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
            // import this module uses (incl. LocalFrnkBottomBarInset). No Material3 / no nav-bar library in
            // commonMain — the engine is `expect`/`actual`, so each platform pulls only what it renders with.
            api(projects.uiScaffolds)
            // Runtime for the generated `Res` accessor (the iOS placeholder drawable). Must be commonMain:
            // the Compose-resources plugin generates the accessor into commonMain even for the single
            // placeholder, so the generated `Res` won't compile if the runtime lives only in iosMain.
            implementation(compose.components.resources)
        }
        androidMain.dependencies {
            // Android bar = a Material3 *Expressive* HorizontalFloatingToolbar (floating pill) over
            // `ImageVector` icons. Pinned to 1.10.0-alpha05 (overrides the 1.9.0 the CMP plugin resolves,
            // which lacks the FloatingToolbar composables). This is the toolkit's SOLE Material3 dependency
            // — quarantined to this module's androidMain. Do not add Material3 to any other module.
            implementation(libs.compose.material3.expressive)
        }
        iosMain.dependencies {
            // iOS bar = the toolkit's own **vendored** native glassy UITabBar (iOS 26+) / Material3 Compose
            // bar (older), under `ui.bottomnav.vendor` — adapted from narendraanjana09's adaptive-nav-bar so
            // the toolkit owns the bar's animations/transitions outright (the published library exposes no
            // animatable hook). Material3 is needed only by the older-iOS fallback (`ComposeNavigationBar`);
            // it was previously transitive from the library. Still quarantined to this one module. Pure
            // Kotlin/Compose (no extra native cinterop), so umbrella XCFrameworks still link under the
            // consumer's existing dynamic_lookup. The vendored fallback reads the bundled placeholder above.
            implementation(libs.compose.material3.expressive)
        }
    }
}
