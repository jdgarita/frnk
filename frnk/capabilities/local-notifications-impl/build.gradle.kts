plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.notifications.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.localNotificationsApi)
            // localNotificationsModule returns a Koin Module — part of the public surface.
            api(libs.koin.core)
        }
        androidMain.dependencies {
            // DatabaseContext — the bootstrap-owned Android Context seam (set by initializeFrnk(context, …)).
            implementation(projects.coreDi)
            implementation(libs.androidx.work.runtime)
        }
    }
}