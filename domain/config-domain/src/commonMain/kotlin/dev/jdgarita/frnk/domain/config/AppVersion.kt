package dev.jdgarita.frnk.domain.config

/**
 * Version representing the app version.  Supports any versioning scheme where there are 3 components.
 */
data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int =
        when {
            major > other.major -> 1
            major < other.major -> -1
            minor > other.minor -> 1
            minor < other.minor -> -1
            patch > other.patch -> 1
            patch < other.patch -> -1
            else -> 0
        }

    override fun toString(): String = "$major.$minor.$patch"
    companion object {
        fun fromString(string: String?): AppVersion? = string?.let {
            it.asAppVersionOrNull()
        }
    }
}

/**
 * Only supports versions that include *exactly* 3 version components - major, minor, and patch.
 */
fun String.asAppVersionOrNull(): AppVersion? =
    try {
        val versions = this.split(".")
        val major = versions[0].toInt()
        val minor = versions[1].toInt()
        val patch = try { versions[2].toInt() } catch (ex: IndexOutOfBoundsException) { 0 }
        AppVersion(major, minor, patch)
    } catch (exception: Exception) {
        null
    }