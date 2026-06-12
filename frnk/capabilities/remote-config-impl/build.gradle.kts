plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.remoteconfig.firebase"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.remoteConfigApi)
            implementation(libs.koin.core)
            implementation(libs.firebase.config)
        }
        // gitlive-firebase 2.x delegates Android transitive versions to the Firebase BOM.
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
    }
}
