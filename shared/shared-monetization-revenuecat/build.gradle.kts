plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.monetization.revenuecat"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sharedMonetizationApi)
            implementation(libs.koin.core)
            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.result)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
