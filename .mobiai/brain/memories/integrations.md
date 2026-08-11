# Integrations

<!--
Notes on third-party integrations (Firebase, analytics, push, payments,
etc.) and their project-specific configuration quirks.
-->

## Firebase (analytics + crash + remote config via gitlive; iOS dSYM)

- id: firebase-integration-analytics-crash-remote-config-via-gitlive-20260612-030809
- type: integration_note
- status: active
- platform: shared
- area: firebase
- date: 2026-06-12

### How frnk uses Firebase
- **Analytics + Crashlytics**: `:analytics-impl` over the GitLive SDKs (`dev.gitlive` firebase-analytics/crashlytics 2.4.0). Exposes `firebaseObservabilityModule`. Install it XOR `noopObservabilityModule` (`:analytics-api`) — never both. Independent axis from the data backend (a local-only app can still ship Firebase telemetry).
- **Remote Config**: its own capability pair `:remote-config-api`/`:remote-config-impl` over `dev.gitlive:firebase-config` 2.4.0 (`Firebase.remoteConfig`). `remoteConfigModule` XOR `noopRemoteConfigModule`. A SIBLING of analytics, kept separate (Stage 11). Replaced the old Firestore-shaped RemoteData stub.
- The toolkit never calls `FirebaseApp.configure()` — the host app does (Android `google-services` plugin; iOS `FirebaseApp.configure()` in Swift + `GoogleService-Info.plist`). On Android the toolkit *does* now supply the build wiring: the **`frnk.android.firebase`** convention plugin (`build-logic`) applies `google-services` + `firebase-crashlytics` to the host's application module, conditional on its `google-services.json`. It still never calls `configure()` — `FirebaseInitProvider` does that from the generated resources.
- **Identity fan-out**: `AnalyticsTracker`, `CrashReporter`, `EntitlementProvider` and `EntitlementManager` all implement `IdentitySource.identify(id)` (`:identity-api`). `DefaultSyncAuthUseCase` resolves the uid and fans it out; **only the billing sink gates the result**, so an unconfigured Firebase degrades telemetry instead of blocking the host.

### iOS crash symbolication (the #1 gotcha)
`firebaseObservabilityModule` installs a CrashKiOS unhandled-exception hook (`enableNativeCrashHandler`, iOS-only) so uncaught Kotlin crashes reach Crashlytics symbolicated. But Crashlytics still needs the matching **dSYM**. Per-app Xcode checklist (toolkit can't wire it): add Firebase via SPM (`FirebaseCrashlytics`) + the plist; `FirebaseApp.configure()` early; ensure Release emits dSYMs (`DEBUG_INFORMATION_FORMAT = dwarf-with-dsym`); add the Crashlytics **run-script build phase** so archives upload dSYMs (SPM path: `"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"`). Umbrella framework is static, so the app's own dSYM already contains frnk's Kotlin frames — no separate Kotlin-framework dSYM, no `crashlyticslink` plugin (that's dynamic-only). "unprocessed — upload 1 dSYM" = Debug build or missing run-script. iosDemoApp has a working example build phase. Details in frnk/capabilities/analytics-impl/CLAUDE.md.

### Demo wiring
Demos use fakes by default (DemoKit stays Firebase-cinterop-free). `demo-android` overrides with the real `firebaseObservabilityModule` + needs `google-services.json`; `iosDemoApp` wires `FirebaseApp.configure()` + plist in Swift.

### Files
- frnk/capabilities/analytics-impl
- frnk/capabilities/remote-config-impl

## RevenueCat (purchases-kmp; Test Store key; SPM RevenueCat product only)

- id: revenuecat-integration-purchases-kmp-test-store-key-spm-revenuecat-20260612-030809
- type: integration_note
- status: active
- platform: shared
- area: revenuecat
- date: 2026-06-12

### How frnk uses RevenueCat
- `:monetization-impl` over `purchases-kmp` 3.0.5 → `revenueCatModule` (the concrete `EntitlementProvider`). `:monetization-api` owns the frnk-side `EntitlementManager` + `FeatureGate` + `DefaultEntitlementManager` (pure Kotlin; overlays a persisted god-mode override so a dev can force Pro even in release). Paywall UI is `:shared-monetization-ui` (`frnkPaywallDestination`, `paywallScaffoldModule`, `rememberFrnkSettingsHandler`) — no RevenueCat dep itself.
- The toolkit never calls `Purchases.configure(...)` — the consumer does. Until configured, `EntitlementManager` degrades to a safe no-op (`isPro == false`); every SDK call is `runCatching`-wrapped.

### iOS consumer setup
1. Add the **`RevenueCat`** Swift package (purchases-ios 5.x compatible with purchases-kmp, e.g. `from: 5.58.0`) via SPM. **Add only the `RevenueCat` product** — skip `RevenueCatUI` (frnk ships its own Compose paywall), `ReceiptParser`, `RevenueCat_CustomEntitlementComputation`.
2. `Purchases.configure(withAPIKey:)` on launch before the gate — a Test Store `test_…` key for testing (project-level, same key iOS+Android), production `appl_…`/`goog_…` for release.
3. Dashboard entitlement id must match `RevenueCatConfig.proEntitlementId` (default `"pro"`); override the Koin binding if it differs.

### Demo + linker
Umbrella frameworks bundling `:monetization-impl` set `linkerOpts("-undefined","dynamic_lookup")` (native purchases-ios supplied by the consumer). DemoKit's iosMain adds the real cinterop; `bootstrapDemoKoinWithRevenueCat(apiKey)` installs the real module over the fake against the Test Store. The RevenueCat frnk-demo project/app/key for the demo smoke test is in the maintainer's global agent memory (the `pro` entitlement still needs manual creation).

### Files
- frnk/capabilities/monetization-impl
- frnk/capabilities/monetization-api
- frnk/capabilities/monetization-ui
