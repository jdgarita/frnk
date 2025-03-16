package dev.jdgarita.frnk.domain.config

import kotlinx.serialization.Serializable

interface ExternalSdkConfig

@Serializable
data class SdkConfiguration(
    val sampleConfig: ExternalSdkConfig? = null
) {

    constructor(config: Map<String, ExternalSdkConfig>) : this(
        sampleConfig = config[KEY_SAMPLE_SDK] as? ExternalSdkConfig
    )

    companion object {
        const val KEY_SAMPLE_SDK = "KEY_SAMPLE_SDK"

        fun default() = SdkConfiguration()
    }
}