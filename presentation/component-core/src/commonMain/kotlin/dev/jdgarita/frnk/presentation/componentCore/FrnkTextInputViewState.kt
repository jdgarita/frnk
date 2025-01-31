package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class FrnkTextInputViewState : ViewState {
    abstract val label: String
    abstract val text: String
    abstract val inputType: FrnkInputType
    abstract val autofillType: FrnkAutofillType?
    abstract val supportingText: String?
    abstract val leadingIcon: SemanticIcon?
    abstract val trailingIcon: SemanticIcon?
    abstract val maxLength: Int?

    /**
     * Merged state for Rest and Focused as focus is handled by the native view component.
     */
    @Serializable
    @SerialName("Default")
    data class Default(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Default
        override val trailingIcon: SemanticIcon = SemanticIcon.Close
    }

    @Serializable
    @SerialName("Error")
    data class Error(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Default
        override val trailingIcon: SemanticIcon = SemanticIcon.Error
    }

    @Serializable
    @SerialName("Numeric")
    data class Numeric(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Number
        override val trailingIcon: SemanticIcon = SemanticIcon.Close
    }

    @Serializable
    @SerialName("NumericError")
    data class NumericError(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Number
        override val trailingIcon: SemanticIcon = SemanticIcon.Error
    }

    @Serializable
    @SerialName("Disabled")
    data class Disabled(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Default
        override val trailingIcon: SemanticIcon? = null
    }

    @Serializable
    @SerialName("Secure")
    sealed class Secure : FrnkTextInputViewState() {
        val visibilityOnIcon: SemanticIcon = SemanticIcon.Visibility
        val visibilityOffIcon: SemanticIcon = SemanticIcon.VisibilityOff
    }

    /**
     * Merged state for Secure.Rest and Secure.Focused as focus is handled by the native view component.
     */
    @Serializable
    @SerialName("SecureDefault")
    data class SecureDefault(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : Secure() {
        override val inputType: FrnkInputType = FrnkInputType.Secure
        override val trailingIcon: SemanticIcon? = null
    }

    @Serializable
    @SerialName("SecureError")
    data class SecureError(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : Secure() {
        override val trailingIcon: SemanticIcon = SemanticIcon.Error
        override val inputType: FrnkInputType = FrnkInputType.Secure
    }

    /**
     * Merged state for Rest and Focused as focus is handled by the native view component.
     */
    @Serializable
    @SerialName("PhoneDefault")
    data class PhoneDefault(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Phone
        override val trailingIcon: SemanticIcon = SemanticIcon.Close
    }

    @Serializable
    @SerialName("PhoneError")
    data class PhoneError(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Phone
        override val trailingIcon: SemanticIcon = SemanticIcon.Error
    }

    /**
     * Merged state for Rest and Focused as focus is handled by the native view component.
     */
    @Serializable
    @SerialName("ScanDefault")
    data class ScanDefault(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null,
        val onScanClick: (() -> Unit)? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Default
        override val trailingIcon: SemanticIcon = SemanticIcon.Scan
    }

    @Serializable
    @SerialName("ScanError")
    data class ScanError(
        override val id: String = ViewState.DEFAULT_ID,
        override val label: String,
        override val text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override val maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null,
        val onScanClick: (() -> Unit)? = null
    ) : FrnkTextInputViewState() {
        override val inputType: FrnkInputType = FrnkInputType.Default
        override val trailingIcon: SemanticIcon = SemanticIcon.Scan
    }

    @Serializable
    @SerialName("Bare")
    data class Bare(
        override val id: String = ViewState.UNIQUE_ID,
        override val label: String = "",
        override var text: String,
        override val supportingText: String? = null,
        override val leadingIcon: SemanticIcon? = null,
        override var maxLength: Int? = null,
        override val autofillType: FrnkAutofillType? = null,
        override val inputType: FrnkInputType = FrnkInputType.Default,
        private val trailingSemanticIcon: SemanticIcon? = null,
        private val onTrailingTapped: ((String) -> Unit),
        val focusChangedAction: ((Boolean) -> Unit),
        val textChangedAction: ((String) -> Unit)
    ) : FrnkTextInputViewState() {
        override val trailingIcon: SemanticIcon get() = trailingSemanticIcon ?: SemanticIcon.Close
        val trailingAction: (() -> Unit) get() = { onTrailingTapped(id) }
    }
}

enum class FrnkInputType {
    Default,
    Secure,
    Phone,
    Scan,
    Number
}

enum class FrnkAutofillType {
    FirstName,
    LastName,
    DateOfBirth,
    Email,
    ZipCode,
    PhoneNumber,
    OTP,
    Password,
    Street,
    City,
    State
}

enum class FakeFrnkTextInputViewState : ViewStateFake<FrnkTextInputViewState> {
    Legacy,
    Bare;

    override val viewState: FrnkTextInputViewState get() =
        when (this) {
            Legacy -> FakeTextInputViewState.legacy
            Bare -> FakeTextInputViewState.bare
        }

    override val id: String get() = viewState.id
}

object FakeTextInputViewState {
    val legacy = FrnkTextInputViewState.Default(
        id = "Legacy",
        label = "Label",
        text = "Text",
        supportingText = "Supporting Text",
        leadingIcon = SemanticIcon.Search
    )

    val bare = FrnkTextInputViewState.Bare(
        id = "Bare",
        label = "Label",
        text = "",
        trailingSemanticIcon = SemanticIcon.Close,
        onTrailingTapped = {},
        textChangedAction = {},
        focusChangedAction = {}
    )
}