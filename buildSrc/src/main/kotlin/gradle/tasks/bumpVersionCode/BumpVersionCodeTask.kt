package gradle.tasks.bumpVersionCode

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.Properties

/**
 * @author Vivien Mahe
 * @since 10/06/2025
 */

@CacheableTask
abstract class BumpVersionCodeTask : DefaultTask() {

    companion object {
        const val VERSION_PROPERTIES_FILE = "version.properties"
        const val VERSION_CODE_PROPERTY = "versionCode"
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val versionPropertiesFile: RegularFileProperty

    init {
        group = "release"
        description = "Bumps the $VERSION_CODE_PROPERTY in $VERSION_PROPERTIES_FILE"
    }

    @TaskAction
    fun bumpVersionCode() {
        val propsFile = versionPropertiesFile.get().asFile

        val props = Properties().apply { load(propsFile.inputStream()) }
        val currentCode = props.getProperty(VERSION_CODE_PROPERTY)?.toIntOrNull()
            ?: error("❌ $VERSION_CODE_PROPERTY not found or invalid in $VERSION_PROPERTIES_FILE")

        val nextCode = currentCode + 1
        props[VERSION_CODE_PROPERTY] = nextCode.toString()
        props.store(propsFile.writer(), null)

        println("✅ Bumped $VERSION_CODE_PROPERTY: $currentCode → $nextCode")
    }
}
