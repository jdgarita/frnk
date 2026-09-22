# iosDemoApp

Internal smoke harness for the Frnk toolkit on iOS. Mirrors `demo-android` —
renders the same Compose-Multiplatform `FrnkDemoApp` from `:demo-shared` and
exercises the MVI engine + `FeatureGate` via fakes.

## Architecture

| Piece | Where |
|---|---|
| Compose UI, MVI ViewModel, Koin module, fakes | `:demo-shared` (commonMain) |
| Swift entry point for Compose | `:demo-shared` (iosMain) `MainViewController()` |
| Framework | `DemoKit.xcframework` (`./gradlew :demo-shared:assembleDemoKitDebugXCFramework`) |
| Xcode app | This folder |

`DemoKit.xcframework` exports the toolkit's **`*-api`** modules plus
`:ui-theme`/`:ui-components`/`:ui-scaffolds` + `:ui-bottom-nav` + `:ui-app`. Its common surface carries
no *optional* `*-impl` module; it references three native SDKs (each supplied by this app via SPM under
`dynamic_lookup`):
- **Sentry + PostHog** — the toolkit's **mandatory** crash-reporting and analytics providers
  (`sentryCrashReportingModule` / `postHogAnalyticsModule`, carried by `:ui-app` for every host),
  installed by `DemoSdksKt.bootstrapDemoKoinWithSdks(...)` from the Swift constants at the top of
  `iosDemoAppApp.swift`. **The DSN and API key are required** — the demo is a real host, there is no
  logging fake, and a blank value fails at bootstrap with a message naming it.
- **RevenueCat** (BACKLOG P3-3, `iosMain` only) — the paywall runs against the RevenueCat **Test
  Store** (real `RevenueCatEntitlementProvider` + `revenueCatIdentityModule`, parity with
  `demo-android`), through the same `bootstrapDemoKoinWithSdks(...)` call.

So this app links the **native Sentry + PostHog + RevenueCat SDKs** (added via SPM, below) plus the
Firebase Apple SDK for `FirebaseApp.configure()` / `GoogleService-Info.plist` (parity with
`demo-android`'s Firebase-backed Remote Config seam; nothing in DemoKit's iOS path reads it today).

For apps that need real backends, build your own umbrella XCFramework over the frnk
modules you use (this demo's `DemoKit` is the worked example) and follow the
integration notes in `docs/HOST_INTEGRATION.md` §6.

## Firebase setup (one-time)

`iosDemoAppApp.swift` calls `FirebaseApp.configure()`, so the Firebase Apple SDK must be linked:

1. In Xcode: **File ▸ Add Package Dependencies…**
2. Enter `https://github.com/firebase/firebase-ios-sdk`, add the package, and add the `FirebaseCore`
   product (the project may still list `FirebaseCrashlytics`, which pulls it transitively — harmless,
   nothing reports to Crashlytics anymore) to the `iosDemoApp` target.
3. `GoogleService-Info.plist` is already bundled (project `frnk-demo`).

## RevenueCat setup (one-time, for the paywall)

The paywall runs against the RevenueCat **Test Store** — no App Store Connect / sandbox
tester needed. The native RevenueCat Apple SDK must be linked into this Xcode project:

1. In Xcode: **File ▸ Add Package Dependencies…**
2. Enter `https://github.com/RevenueCat/purchases-ios.git`, version **`5.58.0` or later**
   (a 5.x compatible with `purchases-kmp` 3.0.5).
3. Add the **`RevenueCat`** product to the `iosDemoApp` target. *(purchases-kmp 3.0+ binds
   directly against `purchases-ios` — **not** `PurchasesHybridCommon`.)*
4. The Test Store `test_` API key is already wired in `iosDemoAppApp.swift`
   (`bootstrapDemoKoinWithSdks`). It's a public key for the throwaway `frnk-demo`
   project; swap it (and the dashboard products/offering) for your own to use a different store.

`Purchases.configure(...)` runs inside the Kotlin bootstrap helper — no Swift configure call needed.

## Sentry + PostHog setup (one-time)

Both packages are already declared in `iosDemoApp.xcodeproj` (`sentry-cocoa` 8.58.x, product
`Sentry`; `posthog-ios` 3.64+, product `PostHog`) — Xcode resolves them on first open. Paste a
Sentry project DSN and a PostHog project API key into the constants at the top of
`iosDemoAppApp.swift` (the same `frnk-demo` project keys `local.properties` uses on Android) —
**both are required**; the app fails at bootstrap without them. Sentry installs its own
unhandled-Kotlin-exception hook, so no extra native wiring is needed.

## Run

1. `xed iosDemoApp.xcodeproj`
2. Select an iPhone simulator (or a device) and ⌘R

The target's first build phase is a Run Script that calls
`./gradlew :demo-shared:assembleDemoKitDebugXCFramework`, so Xcode always picks
up a fresh framework — no manual gradle invocation needed.

## Testing the iOS crash → Sentry

1. Run the app, go to the **Analytics & Crash** section, tap **Force crash (unhandled)**.
2. The app terminates (an uncaught Kotlin exception → Sentry's unhandled-exception hook).
3. **Relaunch the app** — Sentry uploads the pending report on the next launch.
4. Open the Sentry project the DSN belongs to; the crash appears within a few minutes. For
   readable **Kotlin** stack frames, the build's dSYM must be uploaded (`sentry-cli debug-files
   upload`, see `docs/HOST_INTEGRATION.md` §6); otherwise the event still shows but with native
   frames only.

## What the demo demonstrates

- Compose Multiplatform UI rendered inside SwiftUI via
  `UIViewControllerRepresentable` (`ComposeViewController.swift`).
- MVI effects (`DemoEffect.Toast`, `DemoEffect.Navigate`) routed from Compose
  back to a SwiftUI toast overlay (`ContentView.swift`).
- Theming via a `FrnkThemeConfig` demo palette (applied through `:ui-app`'s `FrnkApp`/`FrnkTheme`) —
  the same palette Android uses, defined once in `:demo-shared` commonMain.
- `FeatureGate` exercised against the **real** RevenueCat Test Store provider (offerings,
  sandbox purchase, restore) — plus the frnk-owned **god mode** override (Settings → tap the
  version 7× → Developer), which forces Pro independent of RevenueCat.
