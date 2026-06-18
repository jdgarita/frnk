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
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            // DatabaseContext — the bootstrap-owned Android Context seam.
            implementation(projects.coreDi)
        }
        iosMain.dependencies { implementation(libs.sqldelight.native.driver) }
    }
}