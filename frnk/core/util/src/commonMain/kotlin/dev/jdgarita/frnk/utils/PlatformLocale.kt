package dev.jdgarita.frnk.utils

/**
 * The device's current preferred-language tag (BCP 47-ish, e.g. `"en-US"`, `"es-CR"`, `"es"`),
 * read live from the platform on every call so a system language change is picked up on the next
 * read. Like [PlatformInfo], a narrow `expect/actual` crossing only plain data — no `Context` or
 * composition required, safe from anywhere in common code.
 *
 * Consumers should treat the tag as a hint and match on its primary-language prefix; the toolkit's
 * own consumer is `FrnkLanguage.fromLanguageTag` in `:ui-theme`.
 */
expect fun platformLanguageTag(): String