package dev.jdgarita.frnk.ui.theme

import dev.jdgarita.frnk.utils.platformLanguageTag

/**
 * The languages the toolkit ships default string catalogs for. `FrnkTheme` resolves its string
 * axis from the catalog matching [FrnkThemeConfig.language] — or, when that is `null` (the
 * default), from the device language via [systemFrnkLanguage] — before applying the host's
 * `stringOverrides`, which always win per token.
 *
 * A host that wants an in-app language selector persists its choice and passes it as
 * `FrnkThemeConfig(language = …)`; because the theme builder re-reads the config every
 * composition, switching re-renders all toolkit copy immediately, no restart needed.
 */
enum class FrnkLanguage {
    En,
    Es;

    companion object {
        /**
         * Maps a BCP 47-ish tag (`"es"`, `"es-CR"`, `"en-US"`…) to a supported language by its
         * primary-language prefix; anything unsupported falls back to [En].
         */
        fun fromLanguageTag(tag: String): FrnkLanguage = if (tag.substringBefore('-').equals("es", ignoreCase = true)) Es else En
    }
}

/** The device language as a supported [FrnkLanguage], read live so a system change re-resolves. */
fun systemFrnkLanguage(): FrnkLanguage = FrnkLanguage.fromLanguageTag(platformLanguageTag())