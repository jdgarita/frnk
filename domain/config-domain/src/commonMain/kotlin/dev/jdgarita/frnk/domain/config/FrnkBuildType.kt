package dev.jdgarita.frnk.domain.config

enum class FrnkBuildType {
    Store,
    Dev;

    companion object {
        fun fromString(buildTypeString: String) =
            FrnkBuildType.valueOf(buildTypeString)
    }
}