plugins {
    id("frnk.kmp.library.composehosttest")
}

// :ui-components — the design-system component tier: atoms, molecules, organisms, and the vendored
// placeholder/skeleton machinery, built on compose-unstyled (headless, NOT Material3). Extracted from
// :shared-ui-atoms at restructure Stage 7b; sits above :ui-theme, below :ui-scaffolds. Kotlin packages
// unchanged (dev.jdgarita.frnk.ui.{atoms,molecules,organisms,placeholder}).
//
// The composehosttest convention plugin supplies: frnk.kmp.library.compose, withHostTest { isInclude…},
// the commonDebug @Preview source set + its dependsOn edges, and the androidHostTest test bundle
// (kotlin-test, coroutines-test, compose-ui-test, ui-test-manifest, robolectric) — extracted from this
// module's former hand-written build so :ui-scaffolds shares it.
kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.components"
    }
    sourceSets {
        commonMain.dependencies {
            // Tokens + theme engine; transitively re-exports compose-unstyled `theming` (Theme[...] lookups)
            // and the :haptics contract (LocalFrnkHaptics / HapticType the interactive atoms call).
            api(projects.uiTheme)

            // compose-unstyled headless primitives the atoms wrap. implementation so the consumer surface
            // stays slim (`theming` is exported by :ui-theme).
            implementation(libs.compose.unstyled.primitives)
            implementation(libs.compose.unstyled.button)
            implementation(libs.compose.unstyled.icon)
            implementation(libs.compose.unstyled.separators)
            // Default Lucide vectors referenced directly by the FrnkBottomNavBar @Preview. Hosts override
            // every icon via FrnkThemeConfig, so consumers don't take a Lucide dependency unless they
            // reference Lucide vectors at their own call sites.
            implementation(libs.icons.lucide)
        }
    }
}
