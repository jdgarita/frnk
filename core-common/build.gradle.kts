plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "core_common"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.assertk)
        }
        androidUnitTest.dependencies {
            implementation(libs.junit)
        }
    }
}

android {
    namespace = "dev.jdgarita.frnk.common"
    compileSdk = ProjectConfiguration.Android.COMPILE_SDK
    defaultConfig {
        minSdk = ProjectConfiguration.Android.MIN_SDK
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

buildkonfig {
    packageName = "dev.jdgarita.frnk.common"
    defaultConfigs {
        // Values are surfaced to commonMain so every module that depends on
        // :core-common can read them. Wire real values from local.properties
        // via the buildkonfig DSL once keys are provisioned.
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_URL", "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_ANON_KEY", "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "FIREBASE_API_KEY", "")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "FIREBASE_PROJECT_ID", "")
    }
}
