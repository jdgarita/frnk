plugins {
    id("frnk.kmp.library")
}

kotlin {
    android {
        namespace = "${ProjectConfiguration.GROUP_ID}.utils"
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
