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
            api(libs.koin.android)
            // DatabaseContext lives in the database impl; this is the sanctioned bootstrap-only
            // *-impl import (same role :shared's androidMain initializeFrnk had before Stage 1).
            implementation(projects.sharedDatabaseImpl)
        }
    }
}
