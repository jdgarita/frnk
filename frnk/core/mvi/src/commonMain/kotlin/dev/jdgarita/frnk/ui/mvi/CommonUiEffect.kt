package dev.jdgarita.frnk.ui.mvi

sealed class CommonUiEffect : UiEffect {
    data class DidPressBack(
        val screenId: String? = null
    ) : CommonUiEffect()
}