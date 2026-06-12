package dev.jdgarita.frnk.camera

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Binds the no-op [CameraController] default. A real impl module would override this with a
 * CameraX/AVFoundation binding; today the scaffold ships only the no-op.
 */
val cameraModule: Module =
    module {
        single<CameraController> { NoopCameraController() }
    }
