package dev.jdgarita.frnk.domain.config

import dev.jdgarita.frnk.domain.config.serialization.ClientConfigurationSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ClientConfigurationSerializer::class)
data class ClientConfiguration(
    val siteId: String = error("required"),
    val bannerId: String = error("required"),
    val serviceEnvironment: ServiceEnvironment = error("required")
)