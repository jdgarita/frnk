package dev.jdgarita.frnk.domain.config

import kotlinx.serialization.Serializable

@Serializable
enum class ServiceEnvironment {
    DEV,
    PROD
}

fun String.serviceEnvironmentOrNull(): ServiceEnvironment? =
    try {
        ServiceEnvironment.valueOf(this)
    } catch (exception: Exception) {
        null
    }