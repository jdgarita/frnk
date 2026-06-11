package dev.jdgarita.frnk.database.impl

import dev.jdgarita.frnk.database.KeyValueStore

/** expect — bound per platform. */
expect fun defaultKeyValueStore(): KeyValueStore
