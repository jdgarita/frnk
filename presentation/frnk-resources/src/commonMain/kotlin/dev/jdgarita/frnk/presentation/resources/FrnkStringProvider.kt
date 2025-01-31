package dev.jdgarita.frnk.presentation.resources

interface FrnkStringProvider {
    fun string(resource: Strings): String
    fun formatted(resource: Strings, vararg param: Any): String
    fun plural(resource: Strings, count: Int): String
    fun pluralFormatted(resource: Strings, count: Int, vararg param: Any): String
    fun string(semanticText: SemanticText): String
}