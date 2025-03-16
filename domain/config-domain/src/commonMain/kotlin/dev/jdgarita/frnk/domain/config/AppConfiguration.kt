package dev.jdgarita.frnk.domain.config

import kotlinx.serialization.Serializable

@Serializable
@AppConfiguration
data class AppConfiguration(
    val packageConfiguration: PackageConfiguration,
    val platformConfiguration: PlatformConfiguration,
    val sdkConfiguration: SdkConfiguration = SdkConfiguration.default(),
) {
    val serviceEnvironmentConfiguration: ServiceEnvironmentConfiguration
        get() = when (tenantConfiguration.serviceEnvironment) {
            ServiceEnvironment.DEV -> ServiceEnvironmentConfiguration.DEV
            ServiceEnvironment.PPE -> ServiceEnvironmentConfiguration.PPE
            ServiceEnvironment.PROD -> ServiceEnvironmentConfiguration.PROD
        }

    val buildType: FrnkBuildType
        get() = getBuildType(tenantConfiguration, packageConfiguration)

    companion object {}
}