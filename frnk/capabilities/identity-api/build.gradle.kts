plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.identity"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedUtils)
        }
    }
}