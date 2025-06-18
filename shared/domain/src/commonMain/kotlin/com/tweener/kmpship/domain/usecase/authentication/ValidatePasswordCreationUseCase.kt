package com.tweener.kmpship.domain.usecase.authentication

import com.tweener.kmpship.domain.DomainConstants.PasswordValidation.PASSWORD_REGEX_VALIDATION
import com.tweener.kmpship.domain.DomainConstants.PasswordValidation.PASSWORD_REGEX_VALIDATION_MINIMUM_CHARACTERS
import com.tweener.kmpship.domain.DomainConstants.PasswordValidation.PASSWORD_REGEX_VALIDATION_ONE_DIGIT
import com.tweener.kmpship.domain.DomainConstants.PasswordValidation.PASSWORD_REGEX_VALIDATION_ONE_LETTER
import com.tweener.kmpship.domain.DomainConstants.PasswordValidation.PASSWORD_REGEX_VALIDATION_ONE_SPECIAL_CHARACTER
import com.tweener.kmpship.domain.usecase.SingleUseCase


/**
 * @author Vivien Mahe
 * @since 10/11/2024
 */
class ValidatePasswordCreationUseCase : SingleUseCase<ValidatePasswordCreationUseCase.InputParams, ValidatePasswordCreationUseCase.OutputParams>() {

    class InputParams(
        val password: String,
    )

    class OutputParams(
        val isValid: Boolean,
        val isMinimumCharactersValid: Boolean,
        val isOneLetterValid: Boolean,
        val isOneDigitValid: Boolean,
        val isOneSpecialCharacterValid: Boolean,
    )

    override suspend fun buildUseCase(inputParams: InputParams?): OutputParams {
        val params = assertInputParamsNotNull(inputParams)

        // Validate the whole password
        val passwordRegex = Regex(PASSWORD_REGEX_VALIDATION)
        val isValid = passwordRegex.matches(params.password)

        // Check if the minimum characters rule is met
        val minimumCharactersRuleRegex = Regex(PASSWORD_REGEX_VALIDATION_MINIMUM_CHARACTERS)
        val isMinimumCharactersValid = minimumCharactersRuleRegex.matches(params.password)

        // Check if the password has at least one letter
        val oneLetterRuleRegex = Regex(PASSWORD_REGEX_VALIDATION_ONE_LETTER)
        val isOneLetterValid = oneLetterRuleRegex.matches(params.password)

        // Check if the password has at least one digit
        val oneDigitRuleRegex = Regex(PASSWORD_REGEX_VALIDATION_ONE_DIGIT)
        val isOneDigitValid = oneDigitRuleRegex.matches(params.password)

        // Check if the password has at least one special character
        val oneSpecialCharacterRuleRegex = Regex(PASSWORD_REGEX_VALIDATION_ONE_SPECIAL_CHARACTER)
        val isOneSpecialCharacterValid = oneSpecialCharacterRuleRegex.matches(params.password)

        return OutputParams(
            isValid = isValid,
            isMinimumCharactersValid = isMinimumCharactersValid,
            isOneLetterValid = isOneLetterValid,
            isOneDigitValid = isOneDigitValid,
            isOneSpecialCharacterValid = isOneSpecialCharacterValid,
        )
    }
}
