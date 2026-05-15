# iosDemoApp

Internal smoke harness for the Frnk toolkit on iOS. Mirrors `androidDemoApp` —
renders the same Compose-Multiplatform `DemoScreen` from `:shared-demo`,
exercises the MVI engine + `FeatureGate`, and toggles `BackendChoice` at
runtime to prove Koin re-bootstrap works.

## Architecture

| Piece | Where |
|---|---|
| Compose UI, MVI ViewModel, Koin module, fakes | `:shared-demo` (commonMain) |
| Swift entry point for Compose | `:shared-demo` (iosMain) `MainViewController()` |
| Framework | `DemoKit.xcframework` (`./gradlew :shared-demo:assembleDemoKitDebugXCFramework`) |
| Xcode app | This folder |

`DemoKit` re-exports `:shared`, so Swift gets the whole toolkit surface from a
single `import DemoKit`.

## First run

1. Install CocoaPods if needed: `sudo gem install cocoapods`.
2. Drop a real `GoogleService-Info.plist` into `iosDemoApp/iosDemoApp/`
   (gitignored). Required only if you switch the runtime backend to
   `BackendChoice.Firebase`; Supabase works without it.
3. From `iosDemoApp/`, run:
   ```bash
   pod install
   ```
   This generates `iosDemoApp.xcworkspace` and pulls in `PurchasesHybridCommon`
   (needed because `:shared-monetization-revenuecat`'s cinterop references its
   native symbols) and the Firebase native pods.
4. `xed iosDemoApp.xcworkspace` — opens Xcode against the Pods-aware workspace.
   Do **not** open `iosDemoApp.xcodeproj` directly after `pod install`.
5. Select an iPhone simulator and ⌘R.

The target's first build phase is a Run Script that calls
`./gradlew :shared-demo:assembleDemoKitDebugXCFramework`, so Xcode always picks
up a fresh framework — no manual gradle invocation needed.

## Without pods (CI / quick build smoke)

The project builds without `pod install`:

```bash
cd iosDemoApp
xcodebuild -project iosDemoApp.xcodeproj -scheme iosDemoApp \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  build CODE_SIGNING_ALLOWED=NO
```

Firebase imports are gated behind `#if canImport(FirebaseCore)`. `-undefined
dynamic_lookup` is set on the app target so RevenueCat's native symbols don't
fail at link — they only resolve at app load, so running the app without pods
will work for Supabase but crash the moment RevenueCat or Firebase native code
is touched.

## What the demo demonstrates

- Compose Multiplatform UI rendered inside SwiftUI via
  `UIViewControllerRepresentable` (`ComposeViewController.swift`).
- MVI effects (`DemoEffect.Toast`, `DemoEffect.Navigate`) routed from Compose
  back to a SwiftUI toast overlay (`ContentView.swift`).
- Runtime backend swap (`BackendStore.swift`) — segmented control stops Koin,
  re-bootstraps with the new `BackendChoice`, and re-keys the Compose VC so
  `koinViewModel()` re-resolves against the fresh container.
- Theming via `ProvideToolkitTheme(colors = demoBlueColors())` — same palette
  Android uses, defined once in `:shared-demo` commonMain.
