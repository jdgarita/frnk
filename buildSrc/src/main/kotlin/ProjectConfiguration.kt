object ProjectConfiguration {
    const val GROUP_ID = "dev.jdgarita.frnk"
    const val IOS_FRAMEWORK_NAME = "FrnkKit"

    /** SQLDelight database class name + generated package (see :shared-database-impl). */
    const val DATABASE_NAME = "FrnkDB"
    const val DATABASE_PACKAGE = "dev.jdgarita.frnk.database.sql"

    // SDK floor/ceiling are NOT here: they live in gradle/libs.versions.toml
    // (android-minSdk / android-compileSdk / android-targetSdk) as the single source of truth, read by
    // both the `frnk.kmp.library` convention plugin (build-logic can't see buildSrc) and the special
    // modules' build scripts (`libs.versions.android.compileSdk.get().toInt()`), and inherited by host
    // apps via the shared catalog. Keeping them only in the catalog avoids the two-source drift.
}
