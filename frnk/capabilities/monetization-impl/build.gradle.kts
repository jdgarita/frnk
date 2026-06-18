plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${libs.versions.frnk.groupId.get()}.monetization.revenuecat"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.monetizationApi)
            implementation(libs.koin.core)
            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.result)
        }
    }
}