package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.SqlDriverFactory

/** expect — bound per platform. */
expect fun defaultSqlDriverFactory(): SqlDriverFactory
