package gradle

import java.io.File
import java.util.Properties

object Secrets {
    fun getLocalPropertyOrEnvVar(key: String, rootDir: File): String? {
        return get(key = key, rootDir = rootDir, propertiesFilename = "local.properties")
    }

    fun getVersionPropertyOrEnvVar(key: String, rootDir: File): String? {
        return get(key = key, rootDir = rootDir, propertiesFilename = "version.properties")
    }

    private fun get(key: String, rootDir: File, propertiesFilename: String): String? {
        val localPropertiesFile = File(rootDir, propertiesFilename)
        if (!localPropertiesFile.exists()) return System.getenv(key)

        val props = Properties().apply {
            localPropertiesFile.inputStream().use { load(it) }
        }
        return props.getProperty(key) ?: System.getenv(key)
    }
}
