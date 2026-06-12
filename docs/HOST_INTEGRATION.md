# Host App Integration

`frnk` is a Kotlin Multiplatform toolkit consumed by host apps as a Gradle composite build
(`includeBuild("../frnk")` in the host's `settings.gradle.kts`). The toolkit ships interfaces;
the host wires impls. There are three integration points.

## 0. Module coordinates

Hosts depend on the individual modules they use — there is no aggregator. Android hosts use the
Maven coordinate `dev.jdgarita.frnk:<name>` (composite-build substitution matches by group:name);
the typesafe accessor column is for builds (frnk's own + a host that `includeBuild`s frnk) that read
`projects.<accessor>`. `settings.gradle.kts` is the source of truth for names → dirs.

| Module | Coordinate (`dev.jdgarita.frnk:`) | Accessor | Purpose |
| --- | --- | --- | --- |
| `:core-di` | `core-di` | `projects.coreDi` | Bootstrap: `initializeFrnk(modules)` + `requireFrnkKoin()`. |
| `:shared-utils` | `shared-utils` | `projects.sharedUtils` | Root utils: coroutines, datetime, `AppResult`, `PlatformInfo`, `Frnk.VERSION`. |
| `:core-mvi` | `core-mvi` | `projects.coreMvi` | MVI engine (`MviViewModel`, `UiText`); no Compose. |
| `:core-nav` | `core-nav` | `projects.coreNav` | Navigation3 contract (`ToolkitRoute`, back-stack helpers); no Compose. |
| `:haptics` | `haptics` | `projects.haptics` | `HapticFeedback`/`HapticType` contract + multihaptic engine. |
| `:ui-theme` | `ui-theme` | `projects.uiTheme` | `FrnkTheme` + tokens (compose-unstyled). |
| `:ui-components` | `ui-components` | `projects.uiComponents` | `Frnk*` atoms / molecules / organisms. |
| `:ui-scaffolds` | `ui-scaffolds` | `projects.uiScaffolds` | Page templates + Compose MVI/nav bindings. |
| `:ui-bottom-nav` | `ui-bottom-nav` | `projects.uiBottomNav` | Adaptive bottom nav + `FrnkAppShell`. **Sole Material3 module.** |
| `:ui-app` | `ui-app` | `projects.uiApp` | `FrnkAppScaffold` + `frnkUiModules()`. The batteries-included apex. |
| `:data-db-api` | `data-db-api` | `projects.dataDbApi` | `SqlDriverFactory` SPI (toolkit owns no schema). |
| `:data-db-impl` | `data-db-impl` | `projects.dataDbImpl` | Platform SQLDelight drivers → `databaseModule`. |
| `:data-prefs-api` | `data-prefs-api` | `projects.dataPrefsApi` | `KeyValueStore` + typed `Preference<T>`. |
| `:data-prefs-impl` | `data-prefs-impl` | `projects.dataPrefsImpl` | multiplatform-settings → `prefsModule`. |
| `:analytics-api` | `analytics-api` | `projects.analyticsApi` | `AnalyticsTracker`/`CrashReporter` + `noopObservabilityModule`. |
| `:analytics-impl` | `analytics-impl` | `projects.analyticsImpl` | Firebase analytics + crash → `firebaseObservabilityModule`. |
| `:remote-config-api` | `remote-config-api` | `projects.remoteConfigApi` | `RemoteConfigService` + `noopRemoteConfigModule`. |
| `:remote-config-impl` | `remote-config-impl` | `projects.remoteConfigImpl` | Firebase Remote Config → `remoteConfigModule`. |
| `:camera` | `camera` | `projects.camera` | api-only no-op scaffold → `cameraModule` (no impl yet). |
| `:permissions` | `permissions` | `projects.permissions` | api-only no-op scaffold → `permissionsModule` (no impl yet). |
| `:monetization-api` | `monetization-api` | `projects.monetizationApi` | `EntitlementManager`/`FeatureGate` → `monetizationModule`. |
| `:monetization-impl` | `monetization-impl` | `projects.monetizationImpl` | RevenueCat `EntitlementProvider` → `revenueCatModule`. |
| `:shared-monetization-ui` | `shared-monetization-ui` | `projects.sharedMonetizationUi` | Paywall UI + `paywallScaffoldModule` + `rememberFrnkSettingsHandler`. |

(`:demo-shared` / `:demo-android` and the `iosDemoApp` Xcode target are internal smoke harnesses —
never host-consumable.)

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

## 2. Override UI tokens (colors, typography, strings, icons)

Wrap your host's content in `FrnkTheme` with a `FrnkThemeConfig` (`:ui-theme`). Every `Frnk*`
atom reads styling through `Theme[...]`, so per-token overrides take effect everywhere — supply
only the tokens you want to change (they merge over the bundled defaults):

```kotlin
FrnkTheme(
    config = FrnkThemeConfig(
        lightColorOverrides = mapOf(colorPrimary to MyBrand.Primary),
        textStyleOverrides  = mapOf(titleLarge to MyBrand.Title),
        stringOverrides     = mapOf(stringUpgrade to "Go Pro"),
        iconOverrides       = mapOf(iconBack to MyIcons.Back),
    ),
) {
    MyAppContent()
}
```

`FrnkAppScaffold` / `FrnkAppShell` take the same `themeConfig` and wrap `FrnkTheme` for you.
See `HOST_ALIGNMENT.md` §4–§5 for the full token + icon-pack story.

## 3. Map ToolkitRoute to Compose screens

The toolkit's `ToolkitRoute` (`:core-nav`) is a `@Serializable sealed interface … : NavKey` of
default routes (`Home`, `Settings`, `Onboarding`, `Paywall`). When using `FrnkAppShell` /
`FrnkAppScaffold`, those routes are already wired — you don't map them. For a hand-wired nav3
host, register them on your `FrnkNavDisplay` `entryProvider` and drive navigation through the MVI
effect channel, mutating the host-owned `NavBackStack`:

```kotlin
EffectCollector(viewModel.effects) { effect ->
    when (effect) {
        is MyEffect.Navigate -> backStack.navigateTo(effect.route)   // route: NavKey
        MyEffect.Upgrade     -> backStack.navigateTo(ToolkitRoute.Paywall)
        MyEffect.Back        -> backStack.back()
    }
}
```

See `docs/ARCHITECTURE.md` → Navigation for `frnkNavConfiguration` / `rememberFrnkNavBackStack` /
`FrnkNavDisplay` and the multiple-back-stack `FrnkTabbedNavScaffold`.

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
