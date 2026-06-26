package dev.jdgarita.frnk.database

/**
 * How [SqlDriverFactory.create] reconciles an existing on-disk database with the current schema
 * *before* the driver opens it.
 */
sealed interface SchemaUpgrade {
    /** Open the database as-is — no pre-open wipe (SQLDelight default; use `.sqm` migrations if any). */
    data object None : SchemaUpgrade

    /**
     * Wipe-on-version-bump: a pre-launch alternative to `.sqm` migrations. The factory persists [version]
     * per database name (via the host's [KeyValueStore]); when the persisted value differs from [version]
     * **and** a database file already exists, the factory deletes the file (+ `-wal`/`-shm`) before opening,
     * then records [version]. A bumped [version] therefore drops the old data and recreates the schema fresh.
     *
     * [version] is the host's own schema *generation* counter (bump it on any schema-shape change) — it is
     * independent of SQLDelight's `Schema.version`. Requires a `KeyValueStore` in the graph (install
     * `prefsModule`); the factory throws if one is absent.
     */
    data class WipeOnVersionBump(
        val version: Int
    ) : SchemaUpgrade
}

/**
 * Pure wipe decision for [SchemaUpgrade.WipeOnVersionBump]. Returns `true` when the caller must delete
 * the database file before opening. No side effects — the caller persists [current] after a successful
 * wipe + open (so a process kill mid-wipe can't strand a stale version).
 *
 * Decision table:
 *  - persisted == current             -> no wipe.
 *  - persisted == null + file present -> pre-versioned (old) install -> WIPE.
 *  - persisted == null + no file      -> genuine fresh install -> no wipe.
 *  - persisted  < current             -> upgrade -> WIPE.
 *  - persisted  > current             -> downgrade -> no wipe (open as-is).
 */
fun shouldWipe(
    persisted: Int?,
    current: Int,
    dbFileExists: Boolean
): Boolean =
    when {
        persisted == current -> false
        persisted == null -> dbFileExists
        else -> persisted < current
    }