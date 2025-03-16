package dev.jdgarita.frnk.domain.config

import dev.jdgarita.frnk.domain.config.serialization.PackageConfigurationSerializer
import dev.jdgarita.frnk.util.common.Log
import kotlinx.serialization.Serializable

@Serializable(with = PackageConfigurationSerializer::class)
data class PackageConfiguration(
    val appName: String = error("required"),
    val appId: String = error("required"),
    val versionString: String = error("required"),
    val versionCode: Int = error("required"),
    val appStoreId: String? = null // iOS app store
) {
    init {
        if (versionString.asAppVersionOrNull() == null) {
            Log.e { "Version string should be 3 digits delimited by . $versionString" }
        }
    }
}