plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.core.di"
    }
    sourceSets {
        commonMain.dependencies {
            // initializeFrnk takes/returns Koin types (List<Module>, KoinApplication) — api surface.
            api(libs.koin.core)
        }
        androidMain.dependencies {
            // androidContext(...) for the initializeFrnk(context, modules) overload — absorbs the
            // DatabaseContext.application requirement so Android hosts bootstrap in one call.
            // DatabaseContext itself lives HERE (since restructure Stage 4) so both data impls
            // (:data-db-impl, :data-prefs-impl) can read it without depending on each other.
            api(libs.koin.android)
        }
    }
}
