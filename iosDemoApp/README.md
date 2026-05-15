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

`DemoKit.xcframework` deliberately exports only the toolkit's **`*-api`** modules
plus `shared-ui-atoms` — it does **not** pull in `:shared`. That means no Firebase,
no RevenueCat, no SQLite native code is linked, no `pod install` is required, and
no `GoogleService-Info.plist` is needed. The demo runs out of the box.

For apps that need real backends, depend on `FrnkKit.xcframework` from `:iosApp`
instead and follow the integration notes in `docs/ARCHITECTURE.md`.

## Run

1. `xed iosDemoApp.xcodeproj` (no workspace, no pods)
2. Select an iPhone simulator and ⌘R

The target's first build phase is a Run Script that calls
`./gradlew :shared-demo:assembleDemoKitDebugXCFramework`, so Xcode always picks
up a fresh framework — no manual gradle invocation needed.

## What the demo demonstrates

- Compose Multiplatform UI rendered inside SwiftUI via
  `UIViewControllerRepresentable` (`ComposeViewController.swift`).
- MVI effects (`DemoEffect.Toast`, `DemoEffect.Navigate`) routed from Compose
  back to a SwiftUI toast overlay (`ContentView.swift`).
- Theming via `ProvideToolkitTheme(colors = demoBlueColors())` — same palette
  Android uses, defined once in `:shared-demo` commonMain.
- `FeatureGate` exercised against `FakeEntitlementManager` so the Pro toggle
  works without any subscription SDK.
