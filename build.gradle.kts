plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
}

buildscript {
    dependencies {
        classpath(libs.plugin.kotlin.serialization)
        classpath(libs.plugin.ktlint.gradle)
    }
}

gradle.beforeProject {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(rootProject.libs.versions.plugin.ktLint.jlleitschuh.get())
        filter {
            exclude { element ->
                element.file.path.contains("generated/")
            }
        }
    }
}