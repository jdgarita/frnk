import DemoKit
import FirebaseCore
import SwiftUI

@main
struct iosDemoAppApp: App {
    init() {
        // Firebase must be configured before the CrashKiOS hook can report (BACKLOG P1-5b).
        // Requires the Firebase Apple SDK (added via SPM — see iosDemoApp/README.md) and
        // GoogleService-Info.plist (already bundled).
        FirebaseApp.configure()
        // Install the CrashKiOS unhandled-Kotlin-exception hook so the "Force crash" panic button
        // in the demo is reported to Firebase Crashlytics symbolicated.
        DemoCrashlyticsKt.enableDemoCrashlytics()
        // Bootstrap the demo with the REAL RevenueCat provider over a Test Store key (BACKLOG P3-3) —
        // parity with androidDemoApp, so the paywall shows real offerings and runs sandbox purchases.
        // Requires the RevenueCat Apple SDK via SPM (see iosDemoApp/README.md). This is a PUBLIC Test
        // Store key (test_ prefix) for the throwaway `frnk-demo` project — move it to a gitignored
        // xcconfig / Info.plist value if you'd rather not commit it.
        DemoRevenueCatKt.bootstrapDemoKoinWithRevenueCat(apiKey: "test_qzjfFbAulTeDyCvtBnxOHsrKucH")
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
