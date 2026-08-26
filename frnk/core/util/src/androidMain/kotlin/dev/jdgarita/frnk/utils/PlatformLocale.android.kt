package dev.jdgarita.frnk.utils

import java.util.Locale

actual fun platformLanguageTag(): String = Locale.getDefault().toLanguageTag()