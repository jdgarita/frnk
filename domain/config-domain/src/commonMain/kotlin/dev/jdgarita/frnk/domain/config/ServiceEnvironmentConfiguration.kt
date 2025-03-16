package dev.jdgarita.frnk.domain.config

import dev.jdgarita.frnk.domain.config.serialization.ServiceEnvironmentConfigurationSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ServiceEnvironmentConfigurationSerializer::class)
data class ServiceEnvironmentConfiguration(
    val baseUrl: String = error("required"),
    val type: String = error("required"),
    val staticContentBaseUrl: String = error("required")
) {
    companion object {
        val DEV: ServiceEnvironmentConfiguration =
            ServiceEnvironmentConfiguration(
                baseUrl = "",
                type = "DEV",
                staticContentBaseUrl = ""
            )

        val PROD: ServiceEnvironmentConfiguration =
            ServiceEnvironmentConfiguration(
                baseUrl = "",
                type = "PROD",
                staticContentBaseUrl = ""
            )
    }
}