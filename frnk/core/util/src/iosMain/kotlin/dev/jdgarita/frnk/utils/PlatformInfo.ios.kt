package dev.jdgarita.frnk.utils

import platform.UIKit.UIDevice

actual object PlatformInfo {
    actual val osName: String = UIDevice.currentDevice.systemName

    actual val osVersion: String = UIDevice.currentDevice.systemVersion

    // UIDevice.model is the generic class ("iPhone" / "iPad"), not the marketing name — Apple does
    // not expose the latter publicly. Good enough for a feedback diagnostics line.
    actual val deviceModel: String = UIDevice.currentDevice.model
}