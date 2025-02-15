plugins {
    alias(libs.plugins.kotlin.dsl)
}

group = "dev.jdgarita.frnk"
version = "SNAPSHOT"

dependencies {
    implementation(libs.plugin.kotlin)
}

gradlePlugin {
    plugins.register("dependencies") {
        id = "dependencies"
        implementationClass = "dev.jdgarita.frnk.config.dependencies.DependenciesPlugin"
    }
}