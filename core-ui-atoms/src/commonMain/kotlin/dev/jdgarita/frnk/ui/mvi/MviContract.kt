package dev.jdgarita.frnk.ui.mvi

/**
 * Marker interfaces for the three pillars of a Redux-style MVI screen.
 *  - [UiState]  : immutable snapshot rendered by a Composable.
 *  - [UiAction] : user/system intent that mutates state.
 *  - [UiEffect] : one-shot side effect (navigation, snackbar, toast).
 */
interface UiState
interface UiAction
interface UiEffect
