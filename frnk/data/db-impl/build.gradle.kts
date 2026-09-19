plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.database.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.dataDbApi)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            // The toolkit's driver default: one bundled SQLite build on every platform.
            implementation(libs.androidx.sqlite.bundled)
            // KeyValueStore — the factory persists the schema generation here for
            // SchemaUpgrade.WipeOnVersionBump (resolved leniently via getOrNull in databaseModule).
            implementation(projects.dataPrefsApi)
        }
        androidMain.dependencies {
            // DatabaseContext — the bootstrap-owned Android Context seam.
            implementation(projects.coreDi)
        }
    }
}