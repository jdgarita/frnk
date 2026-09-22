import DemoKit
import SwiftUI

// SDK keys live in Configuration/Secrets.xcconfig (gitignored; copy Secrets.xcconfig.template) —
// the same approach as Faint. Config.xcconfig `#include?`s it, Info.plist forwards each value as
// `$(KEY)`, and DemoKit reads them from the bundle at launch. Nothing to paste here.
@main
struct iosDemoAppApp: App {
    init() {
        // Bootstrap the demo with the REAL providers over the fakes — parity with demo-android.
        // Sentry + PostHog are mandatory (a missing Secrets.xcconfig fails here naming the key);
        // Sentry.init hooks uncaught Kotlin exceptions itself, so the "Force crash" panic button
        // needs no native wiring. No Firebase on iOS: nothing in the demo's iOS path reads it.
        DemoSdksKt.bootstrapDemoKoinWithSdks()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
