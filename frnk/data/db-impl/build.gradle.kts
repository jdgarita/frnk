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
            // KeyValueStore — the factory persists the schema generation here for
            // SchemaUpgrade.WipeOnVersionBump (resolved leniently via getOrNull in databaseModule).
            implementation(projects.dataPrefsApi)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            // DatabaseContext — the bootstrap-owned Android Context seam.
            implementation(projects.coreDi)
        }
        iosMain.dependencies { implementation(libs.sqldelight.native.driver) }
    }
}