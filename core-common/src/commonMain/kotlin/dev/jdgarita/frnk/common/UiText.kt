package dev.jdgarita.frnk.common

/**
 * Platform-agnostic UI string. Use `UiText.Raw` for already-localized
 * messages and `UiText.Resource` for resource-backed strings whose actual
 * rendering happens on the platform side.
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Resource(val key: String, val args: List<Any> = emptyList()) : UiText
}
