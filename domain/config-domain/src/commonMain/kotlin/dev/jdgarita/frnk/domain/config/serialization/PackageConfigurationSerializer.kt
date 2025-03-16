package dev.jdgarita.frnk.domain.config.serialization

import dev.jdgarita.frnk.domain.config.PackageConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

internal object PackageConfigurationSerializer : KSerializer<PackageConfiguration> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "PackageConfiguration",
        serializer<PackageConfiguration>().descriptor
    ) {
        element("appName", String.serializer().descriptor)
        element("appId", String.serializer().descriptor)
        element("versionString", String.serializer().descriptor)
        element("versionCode", Int.serializer().descriptor)
        element("appStoreId", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: PackageConfiguration) {
        encoder.encodeStructure(descriptor) {
            encodeAppName(appName = value.appName)
            encodeAppId(appId = value.appId)
            encodeVersionString(versionString = value.versionString)
            encodeVersionCode(versionCode = value.versionCode)
            encodeAppStoreId(appStoreId = value.appStoreId)
        }
    }

    private fun CompositeEncoder.encodeAppName(appName: String) {
        encodeStringElement(descriptor, 0, appName)
    }

    private fun CompositeEncoder.encodeAppId(appId: String) {
        encodeStringElement(descriptor, 1, appId)
    }

    private fun CompositeEncoder.encodeVersionString(versionString: String) {
        encodeStringElement(descriptor, 2, versionString)
    }

    private fun CompositeEncoder.encodeVersionCode(versionCode: Int) {
        encodeIntElement(descriptor, 3, versionCode)
    }

    private fun CompositeEncoder.encodeAppStoreId(appStoreId: String?) {
        appStoreId?.let {
            encodeStringElement(descriptor, 5, it)
        }
    }

    override fun deserialize(decoder: Decoder): PackageConfiguration {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}