import DemoKit
import FirebaseCore
import SwiftUI

// Real-SDK keys for the iOS demo. All are PUBLIC client keys of the throwaway `frnk-demo` projects;
// move them to a gitignored xcconfig / Info.plist value if you'd rather not commit them.
//  - RevenueCat: a Test Store key (test_ prefix) — routes purchases to the Test Store, no App Store
//    Connect needed (BACKLOG P3-3). Requires the `RevenueCat` SPM product (see README).
//  - Sentry DSN / PostHog key: blank keeps that slot on the demo's logging fake. Requires the `Sentry`
//    and `PostHog` SPM products either way — DemoKit references their symbols under dynamic_lookup.
private let revenueCatApiKey = "test_qzjfFbAulTeDyCvtBnxOHsrKucH"
private let sentryDsn = ""
private let postHogApiKey = ""
private let postHogHost = ""

@main
struct iosDemoAppApp: App {
    init() {
        // Firebase must be configured before the CrashKiOS hook can report (BACKLOG P1-5b).
        // Requires the Firebase Apple SDK (added via SPM — see iosDemoApp/README.md) and
        // GoogleService-Info.plist (already bundled).
        FirebaseApp.configure()
        if sentryDsn.isEmpty {
            // Install the CrashKiOS unhandled-Kotlin-exception hook so the "Force crash" panic button
            // in the demo is reported to Firebase Crashlytics symbolicated. Skipped when Sentry is on:
            // Sentry.init installs its own hook, and two hooks double-report.
            DemoCrashlyticsKt.enableDemoCrashlytics()
        }
        // Bootstrap the demo with the REAL providers over the fakes — parity with androidDemoApp.
        DemoSdksKt.bootstrapDemoKoinWithSdks(
            revenueCatApiKey: revenueCatApiKey,
            sentryDsn: sentryDsn,
            postHogApiKey: postHogApiKey,
            postHogHost: postHogHost
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
