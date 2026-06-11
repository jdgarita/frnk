plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.ui"
    }
    sourceSets {
        commonMain.dependencies {
            // Monetization UI = the design system + the monetization domain. The paywall screen builds on
            // FrnkMviScreen / FrnkScreenScaffold, so it depends on :ui-scaffolds (which re-exports
            // :ui-components + :ui-theme + the MVI/Nav engines transitively) rather than the whole
            // :shared-ui-atoms facade — post-Stage-7 the deps are exactly {ui-scaffolds, monetization-api}
            // (the Stage 8 precondition).
            api(projects.uiScaffolds)
            api(projects.sharedMonetizationApi)
        }
    }
}
