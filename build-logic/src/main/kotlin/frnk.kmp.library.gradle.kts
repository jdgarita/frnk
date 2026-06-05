// frnk KMP-library convention plugin — the standard `shared-*` library shape.
//
// Composes the `frnk.kmp.base` core (KMP + AGP-KMP-library plugins, jvmToolchain, Android SDK) with the
// iOS framework every shared library exposes. A module applies `plugins { id("frnk.kmp.library") }` and
// then declares only what is module-specific: its Android `namespace`, `withHostTest {}` opt-in (or the
// `frnk.kmp.library.hosttest` variant), any extra plugins (compose via `frnk.kmp.library.compose` /
// serialization / sqldelight), and its source-set dependencies.
//
// What stays out of here on purpose:
//  - `namespace` — every module differs and it doesn't map 1:1 from the module name
//    (e.g. :shared-ui-nav → ...ui.bottomnav), so it's set per-module.
//  - `withHostTest {}` — most but not all modules opt in (use `frnk.kmp.library.hosttest`); :shared-ui-atoms
//    needs the `isIncludeAndroidResources = true` variant, configuring it twice would double-register.

plugins {
    id("frnk.kmp.base")
}

kotlin {
    // iOS framework baseName mirrors the long-standing convention `shared_<module>` (underscores),
    // which for every standard module is exactly the Gradle module name with `-` → `_`.
    val frameworkBaseName = project.name.replace('-', '_')
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework { baseName = frameworkBaseName }
    }
}
