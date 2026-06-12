plugins {
    id("frnk.kmp.library.hosttest")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.revenuecat"
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
