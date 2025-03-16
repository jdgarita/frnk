package dev.jdgarita.frnk.domain.config.serialization

import dev.jdgarita.frnk.domain.config.ServiceEnvironmentConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

internal object ServiceEnvironmentConfigurationSerializer : KSerializer<ServiceEnvironmentConfiguration> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "ServiceEnvironmentConfiguration",
        serializer<ServiceEnvironmentConfiguration>().descriptor
    ) {
        element("baseUrl", String.serializer().descriptor)
        element("type", String.serializer().descriptor)
        element("staticContentBaseUrl", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: ServiceEnvironmentConfiguration) {
        encoder.encodeStructure(descriptor) {
            encodeBaseUrl(baseUrl = value.baseUrl)
            encodeType(type = value.type)
            encodeStaticContentBaseUrl(staticContentBaseUrl = value.staticContentBaseUrl)
        }
    }

    private fun CompositeEncoder.encodeBaseUrl(baseUrl: String) {
        encodeStringElement(descriptor, 0, baseUrl)
    }

    private fun CompositeEncoder.encodeType(type: String) {
        encodeStringElement(descriptor, 3, type)
    }

    private fun CompositeEncoder.encodeStaticContentBaseUrl(staticContentBaseUrl: String) {
        encodeStringElement(descriptor, 4, staticContentBaseUrl)
    }

    override fun deserialize(decoder: Decoder): ServiceEnvironmentConfiguration {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}