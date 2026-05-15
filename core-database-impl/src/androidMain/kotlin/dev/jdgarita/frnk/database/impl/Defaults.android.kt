package dev.jdgarita.frnk.database.impl

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.SharedPreferencesSettings
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory

/** Host MUST set this from its Application.onCreate before Koin starts. */
object DatabaseContext {
    lateinit var application: Context
}

actual fun defaultSqlDriverFactory(): SqlDriverFactory =
    SqlDriverFactory { schema, name ->
        AndroidSqliteDriver(schema, DatabaseContext.application, name)
    }

actual fun defaultKeyValueStore(): KeyValueStore {
    val prefs = DatabaseContext.application.getSharedPreferences("frnk_toolkit", Context.MODE_PRIVATE)
    val settings = SharedPreferencesSettings(prefs)
    return SettingsKeyValueStore(settings)
}
