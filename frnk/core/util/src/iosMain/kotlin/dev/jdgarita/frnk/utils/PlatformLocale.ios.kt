package dev.jdgarita.frnk.utils

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

// The first preferred language is what iOS localizes system UI to — a full tag like "es-CR".
// currentLocale.languageCode would ignore the user's app-language ordering, so prefer this.
actual fun platformLanguageTag(): String = NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"