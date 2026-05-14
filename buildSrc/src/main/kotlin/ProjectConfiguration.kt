/**
 * Single source of truth for shared project-level constants. Modules read
 * these values to avoid drift between Android namespaces, package IDs, and
 * iOS framework names.
 */
object ProjectConfiguration {
    const val PACKAGE_NAME = "dev.jdgarita.frnk"
    const val IOS_FRAMEWORK_NAME = "FrnkKit"
    const val DATABASE_CLASS_NAME = "FrnkDB"

    object Android {
        const val MIN_SDK = 26
        const val TARGET_SDK = 35
        const val COMPILE_SDK = 35
    }
}
