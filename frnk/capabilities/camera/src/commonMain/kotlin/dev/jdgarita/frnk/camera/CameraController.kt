package dev.jdgarita.frnk.camera

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

/**
 * Capability **scaffold** (restructure Stage 11 / D4): the minimal cross-platform camera contract.
 * Api-only for now — there is **no** `expect/actual` or native cinterop, only [NoopCameraController].
 * A real CameraX / AVFoundation implementation is future feature work, explicitly out of scope.
 */
interface CameraController {
    /** Capture a single still photo. Returns [AppResult], never throws. */
    suspend fun capturePhoto(): AppResult<CameraImage, CommonError>
}

/**
 * A captured image as raw encoded bytes (e.g. JPEG/PNG) plus its [mimeType]. Kept deliberately tiny
 * — the scaffold doesn't commit to a platform bitmap type.
 */
data class CameraImage(
    val bytes: ByteArray,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CameraImage) return false
        return mimeType == other.mimeType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + mimeType.hashCode()
}