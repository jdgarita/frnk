package dev.jdgarita.frnk.domain.usecase.authentication

import com.tweener.kmpkit.validator.EmailAddressValidator
import dev.jdgarita.frnk.domain.usecase.SingleUseCase

/**
 * @author Vivien Mahe
 * @since 15/07/2024
 */
class ValidateEmailAddressUseCase(
    private val emailAddressValidator: EmailAddressValidator,
) : SingleUseCase<ValidateEmailAddressUseCase.InputParams, ValidateEmailAddressUseCase.OutputParams>() {

    class InputParams(
        val email: String,
    )

    class OutputParams(
        val isValid: Boolean,
    )

    override suspend fun buildUseCase(inputParams: InputParams?): OutputParams {
        val params = assertInputParamsNotNull(inputParams)

        return OutputParams(isValid = emailAddressValidator.isValid(email = params.email))
    }
}
