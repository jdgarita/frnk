import dev.jdgarita.frnk.config.dependencies.Deps
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    val xcf = XCFramework()
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "identity-presentation-api"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(Deps.Main.Frnk.presentationMvi))
                implementation(project(Deps.Main.Frnk.presentationFramework))
            }
        }
    }
}

android {
    namespace = "dev.jdgarita.frnk.presentation.identity.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}