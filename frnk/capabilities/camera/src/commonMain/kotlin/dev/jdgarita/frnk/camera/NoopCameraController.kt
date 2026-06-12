package dev.jdgarita.frnk.camera

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

/**
 * SDK-free default [CameraController]: [capturePhoto] always fails with [CommonError.Unknown] because
 * there is no camera wired. Lets a host compile + run against the contract before a real impl exists;
 * demos surface this as the honest "no camera" outcome.
 */
class NoopCameraController : CameraController {
    override suspend fun capturePhoto(): AppResult<CameraImage, CommonError> = AppResult.Failure(CommonError.Unknown)
}
