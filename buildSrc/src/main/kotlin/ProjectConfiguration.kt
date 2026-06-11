object ProjectConfiguration {
    const val GROUP_ID = "dev.jdgarita.frnk"

    // The toolkit owns NO SQLDelight schema since restructure Stage 4 (OQ-2): the old FrnkDB
    // constants are gone. The demo's DemoDB schema is configured inline in
    // demo/shared/build.gradle.kts — it's demo-only, so it doesn't belong in shared build config.

    // SDK floor/ceiling are NOT here: they live in gradle/libs.versions.toml
    // (android-minSdk / android-compileSdk / android-targetSdk) as the single source of truth, read by
    // both the `frnk.kmp.library` convention plugin (build-logic can't see buildSrc) and the special
    // modules' build scripts (`libs.versions.android.compileSdk.get().toInt()`), and inherited by host
    // apps via the shared catalog. Keeping them only in the catalog avoids the two-source drift.
}
