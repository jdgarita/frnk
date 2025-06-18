import org.gradle.api.JavaVersion

/**
 * @author Vivien Mahe
 * @since 23/07/2022
 */

object ProjectConfiguration {

    object Frnk {
        const val packageName = "dev.jdgarita.frnk"
        const val versionName = "1.0"

        object Android {
            const val applicationId = packageName
            const val namespace = "$packageName.android"
            const val compileSDK = 35
            const val targetSDK = compileSDK
            const val minSDK = 24
        }
    }

    object Compiler {
        val javaCompatibility = JavaVersion.VERSION_21
        val jvmTarget = javaCompatibility.toString()
    }
}
