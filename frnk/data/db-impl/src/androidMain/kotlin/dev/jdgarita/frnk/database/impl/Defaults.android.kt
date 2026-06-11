package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.jdgarita.frnk.database.SqlDriverFactory
import dev.jdgarita.frnk.di.DatabaseContext

actual fun defaultSqlDriverFactory(): SqlDriverFactory =
    SqlDriverFactory { schema, name ->
        AndroidSqliteDriver(schema, DatabaseContext.application, name)
    }
