package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory
import platform.Foundation.NSUserDefaults

actual fun defaultSqlDriverFactory(): SqlDriverFactory = SqlDriverFactory { schema, name ->
    NativeSqliteDriver(schema, name)
}

actual fun defaultKeyValueStore(): KeyValueStore =
    SettingsKeyValueStore(NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults))
