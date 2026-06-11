// :shared-ui-api — transient FACADE (restructure Stage 6).
//
// shared-ui-api was split into :core-mvi (MVI engine + UiText), :core-nav (Compose-free Nav3 contract)
// and :haptics (haptics contract). This module keeps the old `dev.jdgarita.frnk:shared-ui-api`
// coordinate alive — with NO source of its own — so downstream consumers (still + internal modules that
// haven't re-pointed) keep resolving it unchanged. `api(...)` re-exports the three successors, preserving
// the exact transitive classpath shared-ui-api advertised before the split.
//
// Deleted at restructure Stage 9 (the coordinate flip), once still re-points to the new coordinates.
// Parked at frnk/core/ui-api-facade (NOT frnk/core/mvi — that directory is now :core-mvi; two projects
// can't share a projectDir).
plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.ui.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.coreMvi)
            api(projects.coreNav)
            api(projects.haptics)
        }
    }
}
