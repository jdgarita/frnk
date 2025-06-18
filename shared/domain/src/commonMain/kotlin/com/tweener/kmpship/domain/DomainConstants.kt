package com.tweener.kmpship.domain

/**
 * @author Vivien Mahe
 * @since 20/12/2024
 */
object DomainConstants {

    object Payments {

        /**
         * URL to redeem Apple promo codes.
         */
        const val APPLE_REDEEM_URL = "https://apps.apple.com/redeem?ctx=offercodes&id=6657986537"

        /**
         * URL to redeem Apple promo codes with a specific code.
         */
        const val APPLE_REDEEM_WITH_CODE_URL = "$APPLE_REDEEM_URL&code={promo_code}"

        /**
         * URL to redeem Google promo codes.
         */
        const val GOOGLE_REDEEM_URL = "https://play.google.com/redeem"

        /**
         * URL to redeem Google promo codes with a specific code.
         */
        const val GOOGLE_REDEEM_WITH_CODE_URL = "$GOOGLE_REDEEM_URL?code={promo_code}"
    }

    object PasswordValidation {

        /**
         * Password must contain at least 8 characters
         */
        const val PASSWORD_MINIMUM_CHARACTERS = 8

        /**
         * Regex for password characters count validation.
         */
        const val PASSWORD_REGEX_VALIDATION_MINIMUM_CHARACTERS = "^.{$PASSWORD_MINIMUM_CHARACTERS,}$"

        /**
         * Password must contain at least one letter from Latin, Cyrillic, Hiragana, Katakana, or common Chinese characters.
         */
        const val PASSWORD_REGEX_VALIDATION_ONE_LETTER = ".*[A-Za-z\\u00C0-\\u017F\\u0400-\\u04FF\\u3040-\\u30FF\\u4E00-\\u9FFF].*"

        /**
         * Password must contain at least one digit.
         */
        const val PASSWORD_REGEX_VALIDATION_ONE_DIGIT = ".*\\d.*"

        /**
         * Password must contain at least one special character.
         */
        const val PASSWORD_REGEX_VALIDATION_ONE_SPECIAL_CHARACTER = ".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"

        /**
         * Regex for password validation. The password will require the following checks:
         * - Has minimum 8 characters in length. Adjust it by modifying {8,}
         * - At least one uppercase. You can remove this condition by removing (?=.*?[A-Z])
         * - At least one lowercase. You can remove this condition by removing (?=.*?[a-z])
         * - At least one digit. You can remove this condition by removing (?=.*?[0-9])
         * - At least one special character. You can remove this condition by removing (?=.*?[#?!@$%^&*-])
         */
        val PASSWORD_REGEX_VALIDATION =
            "^(?=.*[A-Za-z\\u00C0-\\u017F\\u0400-\\u04FF\\u3040-\\u30FF\\u4E00-\\u9FFF])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
    }

    // TODO Add here constants related to the domain layer (business logic, use cases, etc.).
}
