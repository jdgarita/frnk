package dev.jdgarita.frnk

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion + " from Frnk framework"
}

actual fun getPlatform(): Platform = IOSPlatform()