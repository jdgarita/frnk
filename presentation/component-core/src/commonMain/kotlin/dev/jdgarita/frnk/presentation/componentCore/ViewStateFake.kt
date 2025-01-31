package dev.jdgarita.frnk.presentation.componentCore

interface ViewStateFake<T : ViewState> {
    val viewState: T
    val id: String
}