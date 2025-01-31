package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable

@Serializable
sealed class FrnkAlertViewState : ViewState {
    override val id: String = ViewState.DEFAULT_ID
    open val onTap: (() -> Unit)? = null
}

@Serializable
sealed class ToastAlertViewState : FrnkAlertViewState() {
    data class Success(
        // TODO: Use FrnkTextViewState
        val title: String,
        val subtitle: String? = null,
        val icon: SemanticIcon = SemanticIcon.Check,
        override val id: String = ViewState.DEFAULT_ID,
        override val onTap: (() -> Unit) = { }
    ) : ToastAlertViewState()

    data class Error(
        // TODO: Use FrnkTextViewState
        val title: String,
        val subtitle: String? = null,
        val leadingIcon: SemanticIcon = SemanticIcon.Error,
        val trailingIcon: SemanticIcon? = SemanticIcon.Refresh,
        override val id: String = ViewState.DEFAULT_ID,
        val onTrailingIconClicked: (() -> Unit)? = null
    ) : ToastAlertViewState()

    data class NoInternet(
        override val onTap: (() -> Unit)? = null,
        val onRetryClicked: (() -> Unit)? = null
    ) :
        ToastAlertViewState()
}

@Serializable
sealed class InContentAlertViewState : FrnkAlertViewState() {
    // TODO: Use FrnkTextViewState
    abstract val title: String
    abstract val subtitle: String?
    abstract val icon: SemanticIcon

    data class Success(
        override val title: String,
        override val subtitle: String? = null,
        override val icon: SemanticIcon = SemanticIcon.Check,
        override val id: String = ViewState.DEFAULT_ID
    ) : InContentAlertViewState()

    data class Error(
        override val title: String,
        override val subtitle: String? = null,
        override val icon: SemanticIcon = SemanticIcon.Error,
        override val id: String = ViewState.DEFAULT_ID
    ) : InContentAlertViewState()

    data class Neutral(
        override val title: String,
        override val icon: SemanticIcon,
        override val subtitle: String? = null,
        override val id: String = ViewState.DEFAULT_ID
    ) : InContentAlertViewState()

    data class NotInteractive(
        override val title: String,
        override val icon: SemanticIcon,
        override val subtitle: String? = null,
        override val id: String = ViewState.DEFAULT_ID
    ) : InContentAlertViewState()
}