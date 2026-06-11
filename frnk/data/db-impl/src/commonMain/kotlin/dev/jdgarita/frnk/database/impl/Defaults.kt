package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.SqlDriverFactory

/** expect — bound per platform. */
expect fun defaultSqlDriverFactory(): SqlDriverFactory

expect fun defaultKeyValueStore(): KeyValueStore
