package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlin.experimental.ExperimentalObjCName
import kotlin.js.JsExport
import kotlin.native.ObjCName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalObjCName::class)
@Serializable
sealed class FrnkDialogViewState : ViewState {
    abstract val title: String
    abstract val description: String?
    abstract val buttons: List<ModalButtonViewState>
    abstract val onDismissRequest: () -> Unit

    @ObjCName("modalWithIconHorizontal")
    @Serializable
    @SerialName("ModalWithIconHorizontal")
    data class ModalWithIconHorizontal(
        val icon: SemanticIcon? = null,
        override val id: String = ViewState.DEFAULT_ID,
        override val title: String,
        override val description: String?,
        override val buttons: List<ModalButtonViewState>,
        override val onDismissRequest: () -> Unit
    ) : FrnkDialogViewState()

    @ObjCName("modalWithIconVertical")
    @Serializable
    @SerialName("ModalWithIconVertical")
    data class ModalWithIconVertical(
        val icon: SemanticIcon,
        override val id: String = ViewState.DEFAULT_ID,
        override val title: String,
        override val description: String?,
        override val buttons: List<ModalButtonViewState>,
        override val onDismissRequest: () -> Unit
    ) : FrnkDialogViewState()

    @ObjCName("modalNoIcon")
    @Serializable
    @SerialName("ModalNoIcon")
    data class ModalNoIcon(
        override val id: String = ViewState.DEFAULT_ID,
        override val title: String,
        override val description: String?,
        override val buttons: List<ModalButtonViewState>,
        override val onDismissRequest: () -> Unit
    ) : FrnkDialogViewState()

    @ObjCName("modalWithTextInput")
    @Serializable
    @SerialName("ModalWithTextInput")
    data class ModalWithTextInput(
        override val id: String = ViewState.DEFAULT_ID,
        override val title: String,
        override val description: String?,
        override val onDismissRequest: () -> Unit,
        val ctaButtonText: String,
        val textInput: FrnkTextInputViewState
    ) : FrnkDialogViewState() {
        override val buttons: List<ModalButtonViewState> = listOf()
        val ctaButton: FrnkButtonViewState
            get() = FrnkButtonViewState(
                id = id,
                content = FrnkButtonContent.Text(ctaButtonText),
                style = FrnkButtonStyle.Primary,
                state = FrnkButtonState.Active,
                height = FrnkButtonHeight.Tall
            )
    }
}

@JsExport
@Serializable
data class ModalButtonViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val actionLabel: String,
    val actionIntent: () -> Unit,
    val role: ModalButtonRole? = null
) : ViewState {
    companion object {
        fun defaultButtons(
            cancellationLabel: String,
            confirmationLabel: String,
            onCancellation: () -> Unit,
            onConfirmation: () -> Unit
        ) = listOf(
            ModalButtonViewState(
                id = "ModalButtonViewState.confirmation",
                actionLabel = confirmationLabel,
                actionIntent = onConfirmation,
                role = ModalButtonRole.CONFIRMATION
            ),
            ModalButtonViewState(
                id = "ModalButtonViewState.cancellation",
                actionLabel = cancellationLabel,
                actionIntent = onCancellation,
                role = ModalButtonRole.CANCEL
            )
        )
    }
}

@JsExport
enum class ModalButtonRole {
    CANCEL,
    CONFIRMATION
}