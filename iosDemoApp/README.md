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
`shared-ui-atoms` — it does **not** pull in `:shared`, so no RevenueCat or SQLite
native code is linked. The **one exception** (BACKLOG P1-5b) is the lightweight
**CrashKiOS** cinterop in `iosMain`, added so the demo's "Force crash" panic button
can be reported to Firebase Crashlytics. Because of that, this app now links the
**native Firebase SDK** (added via SPM, below) and ships `GoogleService-Info.plist`.

For apps that need real backends, depend on `FrnkKit.xcframework` from `:iosApp`
instead and follow the integration notes in `docs/ARCHITECTURE.md`.

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
- `FeatureGate` exercised against `FakeEntitlementManager` so the Pro toggle
  works without any subscription SDK.
