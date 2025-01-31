package dev.jdgarita.frnk.presentation.resources

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class SemanticText {
    @Serializable
    data class Raw(val text: String) : SemanticText()

    @Serializable
    data class Res(val resource: Strings) : SemanticText()

    @Serializable
    data class FormattedRes(
        val resource: Strings,
        // We don't really need this class to be Serializable, custom serializer will change it to String,
        @Transient val params: List<Any> = emptyList()
    ) : SemanticText()

    @Serializable
    data class PluralRes(val resource: Strings, val count: Int) : SemanticText()

    @Serializable
    data class PluralFormattedRes(
        val resource: Strings,
        val count: Int,
        // We don't really need this class to be Serializable, custom serializer will change it to String,
        @Transient val params: List<Any> = emptyList()
    ) : SemanticText()
}

fun String.asSemanticText(): SemanticText = SemanticText.Raw(this)
fun Strings.asSemanticText(): SemanticText = SemanticText.Res(this)