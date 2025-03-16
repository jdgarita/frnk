package dev.jdgarita.frnk.domain.config.serialization

import dev.jdgarita.frnk.domain.config.ClientConfiguration
import dev.jdgarita.frnk.domain.config.ServiceEnvironment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

internal object ClientConfigurationSerializer : KSerializer<ClientConfiguration> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "ClientConfiguration",
        serializer<ClientConfiguration>().descriptor
    ) {
        element("siteId", String.serializer().descriptor)
        element("bannerId", String.serializer().descriptor)
        element("serviceEnvironment", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: ClientConfiguration) {
        encoder.encodeStructure(descriptor) {
            encodeSiteId(siteId = value.siteId)
            encodeBannerId(bannerId = value.bannerId)
            encodeServiceEnvironment(serviceEnvironment = value.serviceEnvironment)
        }
    }

    private fun CompositeEncoder.encodeSiteId(siteId: String) {
        encodeStringElement(descriptor, 0, siteId)
    }

    private fun CompositeEncoder.encodeBannerId(bannerId: String) {
        encodeStringElement(descriptor, 1, bannerId)
    }

    private fun CompositeEncoder.encodeServiceEnvironment(serviceEnvironment: ServiceEnvironment) {
        encodeStringElement(descriptor, 2, serviceEnvironment.name)
    }

    override fun deserialize(decoder: Decoder): ClientConfiguration {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}