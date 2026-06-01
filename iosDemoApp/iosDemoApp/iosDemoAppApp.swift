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
        DemoBootstrapKt.bootstrapDemoKoin(extraConfig: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
