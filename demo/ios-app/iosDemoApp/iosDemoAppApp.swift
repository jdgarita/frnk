import DemoKit
import FirebaseCore
import SwiftUI

// Real-SDK keys for the iOS demo. All are PUBLIC client keys of the throwaway `frnk-demo` projects;
// move them to a gitignored xcconfig / Info.plist value if you'd rather not commit them.
//  - RevenueCat: a Test Store key (test_ prefix) — routes purchases to the Test Store, no App Store
//    Connect needed (BACKLOG P3-3). Requires the `RevenueCat` SPM product (see README).
//  - Sentry DSN / PostHog key: REQUIRED. The demo is a real host — observability is always PostHog +
//    Sentry, there is no logging fake — so a blank value fails at bootstrap with a message naming it.
//    Requires the `Sentry` and `PostHog` SPM products (DemoKit references their symbols under
//    dynamic_lookup). Take the values from the same frnk-demo projects local.properties uses on Android.
private let revenueCatApiKey = "test_qzjfFbAulTeDyCvtBnxOHsrKucH"
private let sentryDsn = ""
private let postHogApiKey = ""
private let postHogHost = ""

@main
struct iosDemoAppApp: App {
    init() {
        // Firebase stays configured for parity with demo-android's Firebase-backed seams (Remote
        // Config / Auth); crash reporting is Sentry's, installed by the bootstrap below — Sentry.init
        // hooks uncaught Kotlin exceptions itself, so the "Force crash" panic button needs no native
        // wiring here. Requires the Firebase Apple SDK (SPM) and GoogleService-Info.plist.
        FirebaseApp.configure()
        // Bootstrap the demo with the REAL providers over the fakes — parity with demo-android.
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
