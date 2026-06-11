package dev.jdgarita.frnk.database.impl

import com.russhwolf.settings.NSUserDefaultsSettings
import dev.jdgarita.frnk.database.KeyValueStore
import platform.Foundation.NSUserDefaults

actual fun defaultKeyValueStore(): KeyValueStore = SettingsKeyValueStore(NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults))
