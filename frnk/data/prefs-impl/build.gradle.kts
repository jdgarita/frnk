plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.prefs.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.dataPrefsApi)
            implementation(libs.koin.core)
            implementation(libs.settings.core)
        }
        androidMain.dependencies {
            // DatabaseContext — the bootstrap-owned Android Context seam.
            implementation(projects.coreDi)
        }
        commonTest.dependencies {
            // MapSettings — in-memory Settings for the SettingsKeyValueStore round-trip test.
            implementation(libs.settings.test)
        }
    }
}