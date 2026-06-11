package dev.jdgarita.frnk.database.impl

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.jdgarita.frnk.database.SqlDriverFactory

actual fun defaultSqlDriverFactory(): SqlDriverFactory =
    SqlDriverFactory { schema, name ->
        NativeSqliteDriver(schema, name)
    }
