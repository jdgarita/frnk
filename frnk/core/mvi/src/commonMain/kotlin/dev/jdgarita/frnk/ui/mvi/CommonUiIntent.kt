package dev.jdgarita.frnk.ui.mvi

/**
 * Cross-cutting [UiIntent]s any [MviViewModel] can dispatch — handled by the base's
 * `onIntent(CommonUiIntent)` overload so back handling is wired once instead of per screen.
 */
sealed class CommonUiIntent : UiIntent {
    /** User pressed back. Handled by the base, which emits [CommonUiEffect.DidPressBack]. */
    data object OnBackPressed : CommonUiIntent()
}