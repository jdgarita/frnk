// frnk KMP-library convention plugin — the standard shared-library shape.
//
// Composes the `frnk.kmp.base` core (KMP + AGP-KMP-library plugins, jvmToolchain, Android SDK) with
// bare iOS targets. A module applies `plugins { id("frnk.kmp.library") }` and then declares only what
// is module-specific: its Android `namespace`, `withHostTest {}` opt-in (or the
// `frnk.kmp.library.hosttest` variant), any extra plugins (compose via `frnk.kmp.library.compose` /
// serialization / sqldelight), and its source-set dependencies.
//
// No per-module iOS framework is declared (restructure OQ-3/OQ-4): iOS consumption is umbrella-only —
// the demo links DemoKit (an explicit `XCFramework` in the demo shared module), and each host builds
// its own shared-module framework exporting the frnk modules it uses.
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
    iosArm64()
    iosSimulatorArm64()
}
