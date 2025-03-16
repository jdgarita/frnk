@file:OptIn(ExperimentalObjCName::class)

package dev.jdgarita.frnk.domain.config

import dev.jdgarita.frnk.domain.config.serialization.PlatformConfigurationSerializer
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.serialization.Serializable

@Serializable(with = PlatformConfigurationSerializer::class)
data class PlatformConfiguration(
    val platform: Platform = error("required"),
    val osVersion: String = error("required")
)

enum class Platform {
    Android,

    @ObjCName("iOS")
    IOS
}