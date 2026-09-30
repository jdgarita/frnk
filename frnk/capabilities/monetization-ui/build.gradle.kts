plugins {
    id("frnk.kmp.library.compose")
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.monetization.ui"
        // NoSubscriptionFoundDialogTest drives a real composition (runComposeUiTest) under Robolectric,
        // which needs the merged Android resources/manifest — configured through the host-test
        // compilation frnk.kmp.library.hosttest already created, as :ui-app does.
        compilations.withType(com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation::class.java) {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            // Monetization UI = the design system + the monetization domain. The paywall screen builds on
            // FrnkMviScreen / FrnkScreenScaffold, so it depends on :ui-scaffolds (which re-exports
            // :ui-components + :ui-theme + the MVI/Nav engines transitively) rather than the whole
            // :shared-ui-atoms facade — post-Stage-7 the deps are exactly {ui-scaffolds, monetization-api}
            // (the Stage 8 precondition).
            api(projects.uiScaffolds)
            api(projects.monetizationApi)
            // Multiplatform BackHandler — the hard paywall swallows system back / the iOS back swipe.
            implementation(libs.compose.ui.backhandler)
        }

        // The Compose host-test bundle (kotlin-test + coroutines-test arrive from commonTest via
        // frnk.kmp.library.hosttest), for the dialog's Robolectric compose tests.
        getByName("androidHostTest").dependencies {
            implementation(libs.compose.ui.test)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.robolectric)
        }
    }
}