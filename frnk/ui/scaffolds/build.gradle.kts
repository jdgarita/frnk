plugins {
    id("frnk.kmp.library.composehosttest")
}

// :ui-scaffolds — page templates (Onboarding / Settings / BottomNav / Home / FrnkScreenScaffold) + the
// Compose binding layer for the MVI engine (FrnkMviScreen, EffectCollector) and the Navigation3 engine
// (FrnkNavDisplay, FrnkNavTab, FrnkTabbedBackStacks/-Handler, slide animations).
// Extracted from :shared-ui-atoms at restructure Stage 7b — the Compose-bearing layer above the atoms,
// below the features/bottom-nav. Kotlin packages unchanged (dev.jdgarita.frnk.ui.{scaffolds,mvi,nav}).
kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.ui.scaffolds"
    }
    sourceSets {
        commonMain.dependencies {
            // The atoms/molecules/organisms design system (and, transitively, :ui-theme + :haptics).
            api(projects.uiComponents)
            // The MVI engine (MviViewModel/UiState/…) + the Compose-free Nav3 contract (NavKey/NavBackStack/
            // ToolkitRoute) these Compose bindings bind. api so the facade re-exports them for still.
            api(projects.coreMvi)
            api(projects.coreNav)
            // FeedbackEmail (mailto draft) behind FeedbackEmailLauncher; available transitively via coreMvi
            // but declared directly since used here.
            implementation(projects.sharedUtils)
            // KeyValueStore + Preference<T> backing OnboardingGate (the first-launch gate).
            // implementation (not api): OnboardingGate's constructor is internal, so KeyValueStore
            // never appears in this module's public API and consumers don't need it transitively.
            // Pure stdlib, no SDK/native cinterop, so umbrella XCFrameworks stay clean.
            implementation(projects.dataPrefsApi)
            // ObserveProStatusUseCase — injected leniently into SettingsViewModel so the VM can read
            // Free/Pro internally instead of having it threaded down through Compose. Pure-interface
            // domain module (no billing SDK / no native cinterop). implementation (not api): the VM is
            // constructed only via Koin, so the use case never appears on this module's public surface.
            implementation(projects.monetizationApi)

            // Koin: koinViewModel() in the VM-backed scaffolds, koinEntryProvider() + the navigation<Route>
            // DSL in FrnkNavDisplay. api so hosts build their own graphs/screens without redeclaring.
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            api(libs.koin.navigation3)
            // Lifecycle-aware Compose collection (collectAsStateWithLifecycle / repeatOnLifecycle) powering
            // FrnkMviScreen + EffectCollector. api so hosts inherit it for their own screens.
            api(libs.androidx.lifecycle.runtime.compose)
            // Navigation3 engine: ui = NavDisplay/scene, runtime = NavKey/NavBackStack, viewmodel = the
            // ViewModel-store entry decorator. api so hosts can build their own graphs. Pure Kotlin/Compose,
            // no native cinterop, so umbrella XCFrameworks stay clean.
            api(libs.androidx.navigation3.ui)
            api(libs.androidx.navigation3.runtime)
            api(libs.androidx.navigation3.viewmodel)
            // Multiplatform BackHandler (androidx.compose.ui.backhandler) — used by FrnkTabbedBackHandler to
            // route system/predictive back into the tabbed nav. implementation (internal to the helper).
            implementation(libs.compose.ui.backhandler)
        }

        // HomeViewModelTest constructs a FrnkTopAppBarAction with a Lucide vector directly (the scaffold
        // production code resolves icons from FrnkIcons tokens, so Lucide is test-only here).
        getByName("androidHostTest").dependencies {
            implementation(libs.icons.lucide)
        }
    }
}
