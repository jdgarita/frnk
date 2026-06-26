package dev.jdgarita.frnk.database

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Decision table for [shouldWipe] — the pure core of [SchemaUpgrade.WipeOnVersionBump]. No file IO
 * or persistence here; the impl ([:data-db-impl]) supplies `dbFileExists` and performs the delete.
 */
class ShouldWipeTest {
    @Test
    fun persisted_equals_current_never_wipes() {
        assertFalse(shouldWipe(persisted = 4, current = 4, dbFileExists = true))
        assertFalse(shouldWipe(persisted = 4, current = 4, dbFileExists = false))
    }

    @Test
    fun pre_versioned_install_with_a_file_wipes() {
        // No recorded version but a DB already on disk -> an old, pre-versioning install -> wipe.
        assertTrue(shouldWipe(persisted = null, current = 4, dbFileExists = true))
    }

    @Test
    fun genuine_fresh_install_does_not_wipe() {
        // No recorded version and no file -> first launch ever -> nothing to wipe.
        assertFalse(shouldWipe(persisted = null, current = 4, dbFileExists = false))
    }

    @Test
    fun upgrade_wipes() {
        assertTrue(shouldWipe(persisted = 3, current = 4, dbFileExists = true))
        assertTrue(shouldWipe(persisted = 1, current = 4, dbFileExists = true))
    }

    @Test
    fun downgrade_does_not_wipe() {
        assertFalse(shouldWipe(persisted = 5, current = 4, dbFileExists = true))
    }
}