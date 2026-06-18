package dev.jdgarita.frnk.permissions

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoopPermissionControllerTest {
    private val controller = NoopPermissionController()

    @Test
    fun status_is_not_determined() {
        assertEquals(PermissionStatus.NotDetermined, controller.status(Permission.Camera))
    }

    @Test
    fun request_is_denied() =
        runTest {
            assertEquals(PermissionStatus.Denied, controller.request(Permission.Camera))
        }
}