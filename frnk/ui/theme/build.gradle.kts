plugins {
    id("frnk.kmp.library.compose")
}

// :ui-theme — the toolkit design-system foundation (restructure Stage 7a): tokens (FrnkColors,
// FrnkTypography, FrnkSpacing, FrnkShapes, FrnkIconSize) + the theme engine (FrnkTheme over
// buildPlatformTheme, FrnkThemeConfig, FrnkStrings/FrnkIcons registries, FrnkRipple, the animated
// light/dark appearance). Sits below :ui-components and :ui-scaffolds; depends on :haptics because
// FrnkTheme installs LocalFrnkHaptics so atoms vibrate with zero host wiring (same precedent as the
// ambient ripple). No upward ui deps.
kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.theme"
    }
    sourceSets {
        commonMain.dependencies {
            // FrnkTheme installs LocalFrnkHaptics (multihaptic engine binding). api so the haptics
            // contract + LocalFrnkHaptics flow transitively to :ui-components (atoms call them).
            api(projects.haptics)
            // applyNativeInterfaceStyle (iOS status-bar / appearance plumbing) lives in core-util.
            implementation(projects.sharedUtils)

            // compose-unstyled theming: ThemeProperty / ThemeToken are part of the public token API
            // (FrnkColors etc.), so api. buildPlatformTheme (platformtheme) + the ripple binding are
            // implementation details of FrnkTheme; the Lucide vectors back the FrnkIcons default registry.
            api(libs.compose.unstyled.theming)
            implementation(libs.compose.unstyled.platformtheme)
            implementation(libs.compose.ripple.indication)
            implementation(libs.icons.lucide)
        }
    }
}
