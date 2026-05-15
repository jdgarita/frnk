package dev.jdgarita.frnk.ui

/**
 * Platform-agnostic text holder. The host resolves it against its string catalog so the
 * toolkit can surface errors without coupling to platform resource systems.
 */
sealed interface UiText {
    data class Raw(
        val value: String,
    ) : UiText

    data class Resource(
        val key: String,
        val args: List<Any?> = emptyList(),
    ) : UiText
}
