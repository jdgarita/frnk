package dev.jdgarita.frnk.platform

interface CameraService {
    suspend fun capture(): PlatformResult<EncodedImage>
}

interface ImageSelectionService {
    suspend fun selectImages(limit: Int): PlatformResult<List<ImageReference>>
}

interface ImageDecoder {
    suspend fun decode(reference: ImageReference): PlatformResult<EncodedImage>
}

interface SettingsActions {
    suspend fun openApplicationSettings(): PlatformResult<Unit>

    suspend fun clearLocalData(): PlatformResult<Unit>
}

interface MapsService {
    suspend fun open(
        latitude: Double,
        longitude: Double
    ): PlatformResult<Unit>
}

data class ImageReference(
    val value: String
)

class EncodedImage(
    val bytes: ByteArray,
    val mimeType: String
)

sealed interface PlatformResult<out T> {
    data class Success<T>(
        val value: T
    ) : PlatformResult<T>

    data class Failure(
        val error: PlatformError
    ) : PlatformResult<Nothing>
}

sealed interface PlatformError {
    data object Cancelled : PlatformError

    data object PermissionDenied : PlatformError

    data class Unavailable(
        val reason: String? = null
    ) : PlatformError
}