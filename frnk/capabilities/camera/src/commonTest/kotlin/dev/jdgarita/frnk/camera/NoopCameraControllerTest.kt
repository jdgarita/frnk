package dev.jdgarita.frnk.camera

import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class NoopCameraControllerTest {
    @Test
    fun capturePhoto_fails_without_a_real_camera() =
        runTest {
            assertTrue(NoopCameraController().capturePhoto() is AppResult.Failure)
        }
}
