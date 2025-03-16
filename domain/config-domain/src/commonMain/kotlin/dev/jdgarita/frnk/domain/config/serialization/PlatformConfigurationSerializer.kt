package dev.jdgarita.frnk.domain.config.serialization

import dev.jdgarita.frnk.domain.config.Platform
import dev.jdgarita.frnk.domain.config.PlatformConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

internal object PlatformConfigurationSerializer : KSerializer<PlatformConfiguration> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "PlatformConfiguration",
        serializer<PlatformConfiguration>().descriptor
    ) {
        element("platform", String.serializer().descriptor)
        element("osVersion", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: PlatformConfiguration) {
        encoder.encodeStructure(descriptor) {
            encodePlatform(platform = value.platform)
            encodeOsVersion(osVersion = value.osVersion)
        }
    }

    private fun CompositeEncoder.encodePlatform(platform: Platform) {
        encodeStringElement(descriptor, 0, platform.name)
    }

    private fun CompositeEncoder.encodeOsVersion(osVersion: String) {
        encodeStringElement(descriptor, 1, osVersion)
    }

    override fun deserialize(decoder: Decoder): PlatformConfiguration {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}