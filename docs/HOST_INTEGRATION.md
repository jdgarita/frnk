# Host App Integration

`frnk` is a Kotlin Multiplatform toolkit consumed by host apps as a Gradle composite build
(`includeBuild("../frnk")` in the host's `settings.gradle.kts`). The toolkit ships interfaces;
the host wires impls. There are three integration points.

## 1. Inject your SQLDelight schema

The toolkit owns the **driver factory** (`SqlDriverFactory`, `:data-db-api`, bound by
`databaseModule` from `:data-db-impl`), never a schema. In your host app's DI graph:

```kotlin
val hostDatabaseModule = module {
    single<MyHostDatabase> {
        val factory: SqlDriverFactory = get()
        MyHostDatabase(factory.create(MyHostDatabase.Schema, "host.db"))
    }
}
```

(`demo/shared`'s `demoNotesModule` + `DemoDB` is the worked example of exactly this pattern.)

On Android, before `startKoin { ... }`, point the toolkit at your `Application` context
(`DatabaseContext` lives in `:core-di`, package `dev.jdgarita.frnk.di` — both the SQL driver and
the SharedPreferences-backed `KeyValueStore` resolve through it):

```kotlin
DatabaseContext.application = applicationContext
```

## 2. Override UI tokens (colors, typography, strings)

Wrap your host's content in `ProvideToolkitTheme` with your palette/strings. Every
toolkit atom (`ToolkitButton`, `ToolkitTextField`) reads from these locals automatically.

```kotlin
ProvideToolkitTheme(
    colors = ToolkitColors(primary = MyBrand.Primary, onPrimary = MyBrand.OnPrimary),
    strings = ToolkitStrings(upgrade = "Go Pro"),
) {
    HostNavHost()
}
```

## 3. Map ToolkitRoute to Compose screens

The toolkit emits `ToolkitRoute` values (e.g. when `FeatureGate.requestUpgrade(...)` fires);
the host owns the NavHost and decides what each route renders:

```kotlin
val navigator: Navigator = { route ->
    when (route) {
        ToolkitRoute.Paywall    -> navController.navigate(HostRoutes.Paywall)
        ToolkitRoute.Onboarding -> navController.navigate(HostRoutes.Onboarding)
        ToolkitRoute.Home       -> navController.navigate(HostRoutes.Home)
        else                  -> Unit
    }
}
```

## 4. Bootstrap Koin with an explicit module list

There is no backend/observability/monetization switch — the host passes exactly the toolkit
modules it wants to `initializeFrnk` (`:core-di`, package `dev.jdgarita.frnk.di`). Unwanted
capabilities are simply not installed, so their bindings never appear in the graph.

```kotlin
// Application.onCreate (Android). iOS calls the common overload (no context param).
initializeFrnk(
    context = this,
    modules = frnkUiModules() +                  // :ui-app — scaffold VMs (Home/Settings/Onboarding/BottomNav)
        listOf(
            databaseModule,                      // :data-db-impl — platform SqlDriverFactory (bring your own schema, §1)
            prefsModule,                         // :data-prefs-impl — KeyValueStore (multiplatform-settings)
            firebaseObservabilityModule,         // or noopObservabilityModule (:analytics-api)
            remoteConfigModule,                  // :remote-config-impl — or noopRemoteConfigModule (:remote-config-api); optional
            // Monetization stack (optional — omit all three to run without entitlements):
            revenueCatModule,                    // :monetization-impl — EntitlementProvider
            monetizationModule,                  // :monetization-api — EntitlementManager/FeatureGate
            paywallScaffoldModule,               // :shared-monetization-ui — paywall VM
        ) + hostModules,
)
```

The Android overload also sets `DatabaseContext.application` and registers `androidContext(...)`,
so the section-1 context line is only needed if you bypass `initializeFrnk`. After bootstrap,
`FrnkAppScaffold(appName, appVersion) { /* home items */ }` (`:ui-app`) is the batteries-included
app root; it fails fast with an explanation if `initializeFrnk` didn't run.

Install exactly one observability module (`firebaseObservabilityModule` XOR
`noopObservabilityModule`) — both bind `AnalyticsTracker`/`CrashReporter`. Remote Config follows the
same XOR rule (`remoteConfigModule` from `:remote-config-impl` for the real Firebase backend, XOR
`noopRemoteConfigModule` from `:remote-config-api` to read bundled defaults only). `:camera` /
`:permissions` are api-only scaffolds — install `cameraModule` / `permissionsModule` for their no-op
defaults until a real impl ships.

## 5. Custom analytics

The toolkit fires a generic event vocabulary (`ToolkitEvent.AppOpened`, `PaywallViewed`, …)
through whichever `AnalyticsTracker` is bound. Push your own events through the same instance:

```kotlin
val analytics: AnalyticsTracker by inject()
analytics.trackCustom("Recipe_Saved", mapOf("recipe_id" to id))
```

## 6. iOS: build your own umbrella framework

frnk publishes **no** prebuilt XCFramework (the old `FrnkKit` died with the `:iosApp` aggregator).
An iOS host adds a small KMP "shared" module in its own repo that `api()`-depends on the frnk
modules it uses, `export(...)`s them from an `XCFramework("<YourAppKit>")`, and links that from
Xcode — exactly what the demo does with `DemoKit` (`demo/shared/build.gradle.kts` is the
worked example). Two rules carry over from the old packaging:

- `isStatic = true` + `linkerOpts("-undefined", "dynamic_lookup")` on the framework — bundled impls
  (`:monetization-impl`, `:analytics-impl`) reference native iOS SDKs
  (purchases-ios, Firebase) that **your app** supplies via SPM/CocoaPods; deferred symbol
  resolution lets the framework link without them.
- Don't add `linkerOpts` for specific frameworks — the consumer keeps full control of the native
  dep list.

### RevenueCat consumer setup

The toolkit never calls `Purchases.configure(...)`; the consumer app must:

1. Add the **`RevenueCat`** Swift package (`github.com/RevenueCat/purchases-ios`, a 5.x compatible
   with `purchases-kmp` — e.g. `from: 5.58.0`) via SPM. **Add only the `RevenueCat` product** —
   skip `RevenueCatUI` (frnk ships its own Compose paywall), `ReceiptParser`, and
   `RevenueCat_CustomEntitlementComputation` (mutually-exclusive alternate build).
2. Call `Purchases.configure(withAPIKey:)` on launch before using the gate — a RevenueCat
   **Test Store** `test_…` key for testing (project-level, same key works on iOS and Android), the
   platform's production key (`appl_…` / `goog_…`) for release.
3. Ensure the dashboard entitlement identifier matches `RevenueCatConfig.proEntitlementId`
   (default `"pro"`); override the `RevenueCatConfig` Koin binding if it differs.

Until configured, `EntitlementManager` degrades to a safe no-op (`isPro == false`) — every SDK
call is `runCatching`-wrapped.

### Crashlytics setup (do this for every new iOS app)

> **Claude: when the user is creating/scaffolding a new iOS app on frnk and wants crash reporting,
> walk them through this checklist.** The toolkit cannot wire any of this — it's per-app Xcode
> setup. Skipping step 4 is the #1 reason "crashes don't show up in Crashlytics."

Your umbrella framework is static, so frnk's Kotlin symbols link into the **app binary**.
Installing `firebaseObservabilityModule` adds the CrashKiOS hook that turns an uncaught Kotlin
exception into a Crashlytics report — but Crashlytics still needs the matching **dSYM**:

1. **Add Firebase** to the Xcode project — Firebase Apple SDK via SPM (`FirebaseCrashlytics`
   product) or CocoaPods — and add the app's `GoogleService-Info.plist` to the target.
2. **Configure + install the hook** early at launch: `FirebaseApp.configure()` in Swift, then the
   Kotlin bootstrap with `firebaseObservabilityModule` in the module list.
3. **Confirm Release builds emit dSYMs** — `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` (Xcode's
   Release default; Debug defaults to `dwarf` = **no dSYM**).
4. **Upload dSYMs to Crashlytics** — add the Crashlytics **run-script build phase** so every
   archive uploads automatically (SPM path):
   ```
   "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
   ```
   (`iosDemoApp` has a working example of this build phase — copy its shape.)

KMP specifics: because the umbrella framework is **static**, your app's own dSYM already contains
frnk's Kotlin frames — no separate Kotlin-framework dSYM step, and no CrashKiOS `crashlyticslink`
Gradle plugin (that's only for *dynamic* frameworks). A crash showing as **"unprocessed — upload
1 dSYM file"** means no matching dSYM was uploaded — usually a Debug build or a missing
run-script. Crashes upload on the **next launch**; the first-ever crash can take several minutes
to surface. Details in `frnk/capabilities/analytics-impl/CLAUDE.md`.
