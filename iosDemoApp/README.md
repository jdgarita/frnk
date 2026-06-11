# iosDemoApp

Internal smoke harness for the Frnk toolkit on iOS. Mirrors `androidDemoApp` —
renders the same Compose-Multiplatform `DemoScreen` from `:shared-demo` and
exercises the MVI engine + `FeatureGate` via fakes.

## Architecture

| Piece | Where |
|---|---|
| Compose UI, MVI ViewModel, Koin module, fakes | `:shared-demo` (commonMain) |
| Swift entry point for Compose | `:shared-demo` (iosMain) `MainViewController()` |
| Framework | `DemoKit.xcframework` (`./gradlew :shared-demo:assembleDemoKitDebugXCFramework`) |
| Xcode app | This folder |

`DemoKit.xcframework` exports the toolkit's **`*-api`** modules plus
`shared-ui-atoms` — no `*-impl` modules in its common surface. It links two native SDKs via
`iosMain` cinterops so the demo can exercise the real paths (each supplied by this
app via SPM under `dynamic_lookup`):
- **CrashKiOS** (BACKLOG P1-5b) — the "Force crash" panic button → Firebase Crashlytics.
- **RevenueCat** (BACKLOG P3-3) — the paywall runs against the RevenueCat **Test Store**
  (real `RevenueCatEntitlementProvider`, parity with `androidDemoApp`), via
  `DemoRevenueCatKt.bootstrapDemoKoinWithRevenueCat(apiKey:)` in `iosDemoAppApp.swift`.

So this app links the **native Firebase + RevenueCat SDKs** (added via SPM, below) and
ships `GoogleService-Info.plist`.

For apps that need real backends, build your own umbrella XCFramework over the frnk
modules you use (this demo's `DemoKit` is the worked example) and follow the
integration notes in `docs/HOST_INTEGRATION.md` §6.

## Firebase setup (one-time, for the crash test)

The Crashlytics test needs the native Firebase SDK linked into this Xcode project:

1. In Xcode: **File ▸ Add Package Dependencies…**
2. Enter `https://github.com/firebase/firebase-ios-sdk`, add the package.
3. Add the **`FirebaseCrashlytics`** product (this pulls `FirebaseAnalytics` /
   `FirebaseCore` transitively) to the `iosDemoApp` target.
4. `GoogleService-Info.plist` is already bundled (project `frnk-demo`). Swap it for
   your own Firebase iOS app's plist if you want crashes in your own console.

`FirebaseApp.configure()` + the CrashKiOS hook are already wired in
`iosDemoAppApp.swift`; no further code needed.

## RevenueCat setup (one-time, for the paywall)

The paywall runs against the RevenueCat **Test Store** — no App Store Connect / sandbox
tester needed. The native RevenueCat Apple SDK must be linked into this Xcode project:

1. In Xcode: **File ▸ Add Package Dependencies…**
2. Enter `https://github.com/RevenueCat/purchases-ios.git`, version **`5.58.0` or later**
   (a 5.x compatible with `purchases-kmp` 3.0.5).
3. Add the **`RevenueCat`** product to the `iosDemoApp` target. *(purchases-kmp 3.0+ binds
   directly against `purchases-ios` — **not** `PurchasesHybridCommon`.)*
4. The Test Store `test_` API key is already wired in `iosDemoAppApp.swift`
   (`bootstrapDemoKoinWithRevenueCat`). It's a public key for the throwaway `frnk-demo`
   project; swap it (and the dashboard products/offering) for your own to use a different store.

`Purchases.configure(...)` runs inside the Kotlin bootstrap helper — no Swift configure call needed.

## Run

1. `xed iosDemoApp.xcodeproj`
2. Select an iPhone simulator (or a device) and ⌘R

The target's first build phase is a Run Script that calls
`./gradlew :shared-demo:assembleDemoKitDebugXCFramework`, so Xcode always picks
up a fresh framework — no manual gradle invocation needed.

## Testing the iOS crash → Crashlytics

1. Run the app, go to the **Analytics & Crash** section, tap **Force crash (unhandled)**.
2. The app terminates (an uncaught Kotlin exception → CrashKiOS hook → Crashlytics).
3. **Relaunch the app** — Crashlytics uploads the pending report on the next launch.
4. Open the [Firebase Crashlytics console](https://console.firebase.google.com/)
   for project `frnk-demo`; the crash appears within a few minutes. For readable
   **Kotlin** stack frames, the build's dSYM must be uploaded (Crashlytics' SPM
   `upload-symbols` run-script / `crashlyticslink`); otherwise the event still
   shows but with native frames only.

## What the demo demonstrates

- Compose Multiplatform UI rendered inside SwiftUI via
  `UIViewControllerRepresentable` (`ComposeViewController.swift`).
- MVI effects (`DemoEffect.Toast`, `DemoEffect.Navigate`) routed from Compose
  back to a SwiftUI toast overlay (`ContentView.swift`).
- Theming via `ProvideToolkitTheme(colors = demoBlueColors())` — same palette
  Android uses, defined once in `:shared-demo` commonMain.
- `FeatureGate` exercised against the **real** RevenueCat Test Store provider (offerings,
  sandbox purchase, restore) — plus the frnk-owned **god mode** override (Settings → tap the
  version 7× → Developer), which forces Pro independent of RevenueCat.
