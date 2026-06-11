// :shared-ui-atoms — transient FACADE (restructure Stage 7b).
//
// shared-ui-atoms was split into :ui-theme (tokens + theme engine), :ui-components (atoms/molecules/
// organisms/placeholder) and :ui-scaffolds (scaffolds + Compose MVI/Nav bindings). This module keeps the
// old `dev.jdgarita.frnk:shared-ui-atoms` coordinate alive — with NO source of its own — so downstream
// consumers (still + the internal :shared-ui-nav proof consumer) keep resolving it unchanged. `api(...)`
// re-exports the four successors, preserving the exact transitive classpath shared-ui-atoms advertised
// before the split (compose runtime/foundation/ui via the .compose plugins on the successors; koin, nav3,
// lifecycle via :ui-scaffolds; compose-unstyled theming via :ui-theme; the MVI/Nav contracts via core-*).
//
// Deleted at restructure Stage 9 (the coordinate flip), once still re-points to the new coordinates.
// Parked at frnk/ui/atoms-facade (NOT frnk/ui/components — that directory is now :ui-components; two
// projects can't share a projectDir).
plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.atoms"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.uiTheme)
            api(projects.uiComponents)
            api(projects.uiScaffolds)
            // haptics is reachable transitively via :ui-theme, but exported explicitly to mirror the
            // pre-split classpath (shared-ui-atoms api'd it through :shared-ui-api).
            api(projects.haptics)
        }
    }
}
