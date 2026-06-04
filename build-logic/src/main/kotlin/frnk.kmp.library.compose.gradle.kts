import org.jetbrains.compose.ComposeExtension
import org.gradle.kotlin.dsl.getByType

// frnk KMP-library + Compose convention plugin.
//
// Composes the `frnk.kmp.library` base with the Compose Multiplatform plugin pair AND the baseline
// `api(compose.runtime/foundation/ui)` exports every Compose module needs — so a Compose-bearing module
// declares `plugins { id("frnk.kmp.library.compose") }` instead of repeating the base id, the
// `compose.multiplatform` + `kotlin.compose` aliases, and the three `api(compose.*)` lines. Plugin
// application is idempotent, so a module can also apply `id("frnk.kmp.library.hosttest")` alongside this —
// the base plugin is still applied once.

plugins {
    id("frnk.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The Compose dependency notations (`compose.runtime`, …) come from the just-applied compose plugin's
// extension; expose them as `api` so they're on every Compose module's public surface uniformly (rather
// than each module re-declaring them, or relying on a transitive export from a sibling).
private val compose = extensions.getByType<ComposeExtension>().dependencies

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
        }
    }
}
