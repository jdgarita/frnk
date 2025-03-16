package dev.jdgarita.frnk.domain.config

import dev.jdgarita.frnk.domain.config.ext.getBuildType
import kotlinx.serialization.Serializable

@Serializable
data class AppConfiguration(
    val packageConfiguration: PackageConfiguration,
    val platformConfiguration: PlatformConfiguration,
    val clientConfiguration: ClientConfiguration,
    val sdkConfiguration: SdkConfiguration = SdkConfiguration.default()
) {
    val serviceEnvironmentConfiguration: ServiceEnvironmentConfiguration
        get() = when (clientConfiguration.serviceEnvironment) {
            ServiceEnvironment.DEV -> ServiceEnvironmentConfiguration.DEV
            ServiceEnvironment.PROD -> ServiceEnvironmentConfiguration.PROD
        }

    val buildType: FrnkBuildType
        get() = getBuildType(clientConfiguration)

    companion object
}