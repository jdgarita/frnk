package dev.jdgarita.frnk.database.impl

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.di.DatabaseContext

actual fun defaultKeyValueStore(): KeyValueStore {
    val prefs = DatabaseContext.application.getSharedPreferences("frnk_toolkit", Context.MODE_PRIVATE)
    val settings = SharedPreferencesSettings(prefs)
    return SettingsKeyValueStore(settings)
}
