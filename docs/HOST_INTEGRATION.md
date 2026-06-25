# Host App Integration

`frnk` is a Kotlin Multiplatform toolkit consumed by host apps as a Gradle composite build
(`includeBuild("../frnk")` in the host's `settings.gradle.kts`). The toolkit ships interfaces and a
design system; the host owns the application, wires impls, overrides tokens, and extends the DI graph.

This is the single canonical host-integration guide. `docs/ARCHITECTURE.md` is the module graph; this
doc is the recipe. (The former `HOST_ALIGNMENT.md` was folded in here — §7–§10 below.)

## 0. Module coordinates

Hosts depend on the individual modules they use — there is no aggregator. Android hosts use the
Maven coordinate `dev.jdgarita.frnk:<name>` (composite-build substitution matches by group:name);
the typesafe accessor column is for builds (frnk's own + a host that `includeBuild`s frnk) that read
`projects.<accessor>`. `settings.gradle.kts` is the source of truth for names → dirs.

| Module | Coordinate (`dev.jdgarita.frnk:`) | Accessor | Purpose |
| --- | --- | --- | --- |
| `:core-di` | `core-di` | `projects.coreDi` | Bootstrap: `initializeFrnk(modules, validate, validator)` + `requireFrnkKoin()`. |
| `:shared-utils` | `shared-utils` | `projects.sharedUtils` | Root utils: coroutines, datetime, `AppResult`, `PlatformInfo`, `Frnk.VERSION`. |
| `:core-mvi` | `core-mvi` | `projects.coreMvi` | MVI engine (`MviViewModel`, `UiText`); no Compose. |
| `:core-nav` | `core-nav` | `projects.coreNav` | Navigation3 contract (`FrnkTabRoute` / `FrnkRootRoute`, back-stack helpers); no Compose. |
| `:haptics` | `haptics` | `projects.haptics` | `HapticFeedback`/`HapticType` contract + multihaptic engine. |
| `:ui-theme` | `ui-theme` | `projects.uiTheme` | `FrnkTheme` + tokens (compose-unstyled). |
| `:ui-components` | `ui-components` | `projects.uiComponents` | `Frnk*` atoms / molecules / organisms. |
| `:ui-scaffolds` | `ui-scaffolds` | `projects.uiScaffolds` | Page templates + Compose MVI/nav bindings. |
| `:ui-bottom-nav` | `ui-bottom-nav` | `projects.uiBottomNav` | Adaptive bottom nav + `FrnkNestedNavScaffold` (the multiple-back-stack tabbed scaffold). **Sole Material3 module.** |
| `:ui-app` | `ui-app` | `projects.uiApp` | `FrnkApp` + `frnkUiModules()` + `frnkModules { }` builder + `Koin::validateFrnkBootstrap`/`checkFrnkModules()` (§4.1). The app-root apex. |
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
`databaseModule` from `:data-db-impl`), never a schema. In your host app's DI graph, use the
`databaseSingle` Koin helper (`:data-db-api`) — it resolves the `SqlDriverFactory`, calls
`create(schema, name)`, and registers the result as a `single<MyHostDatabase>`:

```kotlin
val hostDatabaseModule = module {
    databaseSingle(MyHostDatabase.Schema, "host.db") { driver -> MyHostDatabase(driver) }
}
```

(`demo/shared`'s `demoNotesModule` + `DemoDB` is the worked example.) The raw long form stays valid
for anything the helper doesn't cover:

```kotlin
single<MyHostDatabase> { MyHostDatabase(get<SqlDriverFactory>().create(MyHostDatabase.Schema, "host.db")) }
```

On Android, before `startKoin { ... }`, point the toolkit at your `Application` context
(`DatabaseContext` lives in `:core-di`, package `dev.jdgarita.frnk.di` — both the SQL driver and
the SharedPreferences-backed `KeyValueStore` resolve through it):

```kotlin
DatabaseContext.application = applicationContext
```

(The Android `initializeFrnk(context, …)` overload in §4 does this for you.)

## 2. Override UI tokens (colors, typography, spacing, shapes, strings, icons)

Wrap your host's content in `FrnkTheme` with a `FrnkThemeConfig` (`:ui-theme`). Every design axis
has bundled defaults and is host-overridable through this single entry point. Every `Frnk*` atom
reads styling through `Theme[...]`, so per-token overrides take effect everywhere — supply only the
tokens you want to change (they merge over the defaults via `Map.plus`; host values win):

```kotlin
FrnkTheme(
    config = FrnkThemeConfig(
        lightColorOverrides = mapOf(colorPrimary to Brand.Primary, colorPrimaryContainer to Brand.Tint),
        darkColorOverrides  = mapOf(colorPrimary to Brand.PrimaryDark),
        textStyleOverrides  = mapOf(titleLarge to Brand.Title),
        fontFamily          = Brand.Inter,                 // restyle ALL text styles at once
        shapeOverrides      = mapOf(shapeButton to RoundedCornerShape(8.dp)),
        spacingOverrides    = mapOf(spacingMd to 12.dp),   // dimens/padding axis
        iconSizeOverrides   = mapOf(iconSizeMd to 28.dp),  // icon-size axis
        stringOverrides     = mapOf(stringUpgrade to "Go Pro"),
        iconOverrides       = mapOf(iconBack to Brand.Icons.Back),
    ),
) {
    MyAppContent()
}
```

`FrnkApp` wraps `FrnkTheme` for you, with the `AppearanceController`-driven light/dark palette. Pass your
host token overrides straight through its `themeConfig: FrnkThemeConfig` parameter (§8) — `FrnkApp` hands it
to the `FrnkTheme` it owns, so the whole app picks them up (default `FrnkThemeConfig.Default` leaves the
bundled palette unchanged). Only hosts that hand-wire the nav primitives without `FrnkApp` wrap their own
`FrnkTheme(config) { … }`.

**Custom icon pack.** The toolkit ships a default Lucide-backed icon registry (`iconBack`, `iconClose`,
`iconSearch`, `iconSettings`, …). A host overrides any or all of them — **or adds brand-specific icons** —
through `iconOverrides`. `Theme[icons][token]` resolves overrides transparently at every call site, so
atoms don't know or care whether an icon is a Lucide default or a host glyph. Hosts that never reference
Lucide icons by name don't take the Lucide dependency at all.

```kotlin
FrnkThemeConfig(
    iconOverrides = mapOf(
        iconBack     to MyIcons.ChevronLeft,   // override a generic default with a brand glyph
        iconUpgrade  to MyIcons.Crown,
        myBrandToken to MyIcons.Rocket,        // brand-only token the host declares itself
    ),
)
```

## 3. Map routes to Compose screens

The toolkit ships **two** `@Serializable sealed interface … : NavKey` route catalogues (`:core-nav`):
**`FrnkTabRoute`** keys the **nested/tab** stack — the fixed three tabs (`Home`, `Settings`, `Custom`) — and
**`FrnkRootRoute`** keys the **root** stack and owns the full-screen flows (`Onboarding`, `Paywall`, plus the
`Tab` shell). Reach for `FrnkRootRoute` for anything above the bottom bar. The host wires them itself:
register them on your `FrnkNavDisplay` `entryProvider` (or, with `FrnkApp` / `FrnkNestedNavScaffold` (§8), in
the Koin `navigation<Route> { … }` module you hand the scaffold) and drive navigation through the MVI effect
channel, mutating the host-owned `NavBackStack`:

```kotlin
// FrnkScreen consumes the VM's one-shot effects via its single-consumer onEffect:
FrnkScreen(
    viewModel = viewModel,
    arguments = MyArgs,
    onEffect = { effect ->
        when (effect) {
            is MyEffect.Navigate -> backStack.navigateTo(effect.route)   // route: NavKey
            MyEffect.Upgrade     -> backStack.navigateTo(FrnkRootRoute.Paywall)
            MyEffect.Back        -> backStack.back()
        }
    }
) { state -> /* render with Frnk* atoms */ }
```

See `docs/ARCHITECTURE.md` → Navigation for `frnkRootNavConfig(...)` / `frnkNestedNavConfig(...)` /
`rememberFrnkNavBackStack` / `FrnkNavDisplay` and the multiple-back-stack `FrnkNestedNavScaffold`.

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
        ) + hostModules,                         // your repositories, feature VMs, schema module — after the toolkit's
) {
    // extraConfig: Koin DSL escape hatch (logging, overrides).
    // allowOverride(true)  // lets a host module override a toolkit binding (e.g. a custom EntitlementProvider)
}
```

- **Host modules** go in the same list, **after** the toolkit's — so with `allowOverride(true)` a
  host can override a toolkit binding (a custom `SqlDriver` schema, a custom `EntitlementProvider`).
- The Android overload also sets `DatabaseContext.application` and registers `androidContext(...)`,
  so the §1 context line is only needed if you bypass `initializeFrnk`.
- **Monetization opt-out:** don't pass the three monetization modules. A host using a different
  provider passes its own `EntitlementProvider` (optionally with the toolkit's `monetizationModule` /
  `paywallScaffoldModule` over it).
- Install **exactly one** observability module (`firebaseObservabilityModule` XOR
  `noopObservabilityModule`) — both bind `AnalyticsTracker`/`CrashReporter`. Remote Config follows the
  same XOR rule (`remoteConfigModule` XOR `noopRemoteConfigModule`). `:camera` / `:permissions` are
  api-only scaffolds — install `cameraModule` / `permissionsModule` for their no-op defaults until a
  real impl ships.

**iOS** (on launch, via a Kotlin bootstrap function your umbrella shared module exposes to Swift —
the common `initializeFrnk(modules)` overload, no context param):

```kotlin
fun bootstrapMyAppKit(): KoinApplication =
    initializeFrnk(modules = frnkUiModules() + databaseModule + prefsModule + /* … */ myAppModules)
```

After bootstrap, `FrnkApp(onSavedStateConfiguration, onNavigationModule)` (§8) is the app root; it fails
fast with an explanation if `initializeFrnk` didn't run.

### 4.1 Optional: the `frnkModules { }` builder + `validate = true` (Tier 2.2)

The explicit list above stays the canonical path. Two **additive** opt-ins (both in `:ui-app`) remove the
two footguns it leaves to host discipline:

```kotlin
initializeFrnk(
    context = this,
    modules = frnkModules {
        observability = firebaseObservabilityModule   // single slot ⇒ XOR by construction
        remoteConfig = remoteConfigModule             // single slot ⇒ XOR by construction
        monetization(provider = revenueCatModule)     // bundles monetizationModule + paywallScaffoldModule
        modules(databaseModule, prefsModule, *hostModules.toTypedArray())
    },
    validate = true,
    validator = Koin::validateFrnkBootstrap,
)
```

- **`frnkModules { }`** assembles the list. `observability`/`remoteConfig` are single slots (default to the
  no-op modules), so installing two — the silent-shadowing footgun — is **unrepresentable**; `monetization(provider)`
  auto-bundles the trio so you can't forget `monetizationModule`/`paywallScaffoldModule`; `frnkUiModules()` is
  always included. You still import the impl `val`s yourself and assign them (the builder never references an
  `*-impl` module, so the toolkit stays cinterop-clean).
- **`validate = true` + `validator = Koin::validateFrnkBootstrap`** runs a post-`startKoin` check that throws a
  message naming the exact missing module (one observability, one remote-config, the monetization stack the
  Settings scaffold needs). This catches the *missing-module* footgun on **either** path — it works with a raw
  `initializeFrnk(modules = …)` list too. Note it runs after start, so it can detect a *missing* module but **not**
  a *duplicate* one (two observability modules collapse to one binding) — that's what the builder's single slots
  prevent. `KeyValueStore`/`SqlDriverFactory` are treated as optional (a local-only host omits them).

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
Xcode. `demo/shared/build.gradle.kts` is the worked example; everything below is lifted from it and
generalized so you can copy it without opening the demo.

Two rules carry over from the old packaging:

- `isStatic = true` + `linkerOpts("-undefined", "dynamic_lookup")` on the framework — bundled impls
  (`:monetization-impl`, `:analytics-impl`) reference native iOS SDKs
  (purchases-ios, Firebase) that **your app** supplies via SPM/CocoaPods; deferred symbol
  resolution lets the framework link without them.
- Don't add `linkerOpts` for specific frameworks — the consumer keeps full control of the native
  dep list.

### The umbrella module's `build.gradle.kts`

`export(...)` is **non-transitive**, so list every frnk module whose Swift API you want visible —
depending on it via `api(...)` is not enough. Each exported module must *also* be a direct `api(...)`
dependency.

```kotlin
kotlin {
    val xcf = XCFramework("YourAppKit")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { t ->
        t.binaries.framework {
            baseName = "YourAppKit"
            xcf.add(this)
            isStatic = true
            // Export every frnk module whose symbols Swift calls (export is non-transitive).
            export(projects.sharedUtils)
            export(projects.coreMvi)
            export(projects.coreNav)
            export(projects.haptics)
            export(projects.uiTheme)
            export(projects.uiComponents)
            export(projects.uiScaffolds)
            export(projects.uiBottomNav)
            export(projects.analyticsApi)
            export(projects.dataDbApi)
            export(projects.dataPrefsApi)
            export(projects.monetizationApi)
            export(projects.sharedMonetizationUi)
            export(projects.uiApp)
            // Defer native RevenueCat/Firebase symbols to the app's own link step.
            linkerOpts("-undefined", "dynamic_lookup")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // One api(...) per export(...) above.
            api(projects.sharedUtils)
            // …
            api(projects.uiApp)
        }
    }
}
```

Keep the **common** surface free of `*-impl` modules so the framework links no native cinterop. If you
need real crash reporting or RevenueCat on iOS, add those impls **only** to `iosMain` (the demo adds
`crashkios.crashlytics` + `monetizationImpl` + `revenuecat.core` there) — the consumer Xcode project
then supplies the matching native SDKs via SPM, and the `dynamic_lookup` flag above lets the framework
link locally without them.

Build it with the generated task `assemble<Name>{Debug,Release}XCFramework` — e.g.
`./gradlew :your-shared:assembleYourAppKitDebugXCFramework`.

### Xcode build phases

Two **Run Script** phases wire the framework + symbolication into the Xcode build (the demo's
`iosDemoApp.xcodeproj` is the working reference). Add both with **"Based on dependency analysis"
unchecked** so they run every build.

**1. Build the XCFramework** — place this *above* "Compile Sources" so the framework exists before the
app compiles:

```sh
set -e
cd "$SRCROOT/../.."   # adjust to your repo root
if [ "$CONFIGURATION" = "Release" ]; then
  ./gradlew :your-shared:assembleYourAppKitReleaseXCFramework
else
  ./gradlew :your-shared:assembleYourAppKitDebugXCFramework
fi
```

**2. Upload Crashlytics dSYMs** — the last phase; uploads the app dSYM (which, because the framework
is static, already carries frnk's Kotlin frames) so crashes symbolicate:

```sh
# Requires DEBUG_INFORMATION_FORMAT=dwarf-with-dsym (Xcode's Release default).
set -e
TOOL="${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols"
GSP="${SRCROOT}/YourApp/GoogleService-Info.plist"
DSYM="${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}"
if [ -f "$TOOL" ] && [ -d "$DSYM" ]; then
  "$TOOL" -gsp "$GSP" -p ios "$DSYM" || echo "warning: Crashlytics dSYM upload failed (continuing)"
else
  echo "warning: Crashlytics upload-symbols or dSYM not found - skipping (add FirebaseCrashlytics via SPM + enable dSYM)"
fi
```

The `GoogleService-Info.plist` itself is added to the app target as a normal bundled resource — no copy
phase needed.

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

### Gating features with `FeatureGate` (Tier 3.1)

`Feature` is an **open marker interface** (`interface Feature { val id: String }`), so gates are
type-safe and host-extensible — there's no public `Feature("…")` constructor to fat-finger. The toolkit
ships its own catalogue as `enum class FrnkFeature(override val id) : Feature { Premium, … }`; declare
your own the same way:

```kotlin
enum class AppFeature(override val id: String) : Feature {
    CloudSync("cloud_sync"),
    DarkThemes("dark_themes")
}

// resolve the FeatureGate from Koin, then:
if (gate.canUse(AppFeature.CloudSync)) sync() else navigate(gate.requestUpgrade(source = "sync_button"))
```

`canUse` / `observe` return Pro-or-free; `requestUpgrade(source)` logs `Paywall_Viewed` and returns the
toolkit paywall route key. `FeatureGate(freeFeatures = …)` whitelists features for Free users (matched by
`Feature.id`) — but note it's **not wired through `monetizationModule`** today (binds the empty default),
so configuring it means overriding the `FeatureGate` Koin binding.

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
4. **Upload dSYMs to Crashlytics** — add the dSYM-upload run-script build phase from
   [Xcode build phases](#xcode-build-phases) above (phase 2) so every build/archive uploads
   automatically. Skipping this is the #1 cause of unsymbolicated crashes.

KMP specifics: because the umbrella framework is **static**, your app's own dSYM already contains
frnk's Kotlin frames — no separate Kotlin-framework dSYM step, and no CrashKiOS `crashlyticslink`
Gradle plugin (that's only for *dynamic* frameworks). A crash showing as **"unprocessed — upload
1 dSYM file"** means no matching dSYM was uploaded — usually a Debug build or a missing
run-script. Crashes upload on the **next launch**; the first-ever crash can take several minutes
to surface. Details in `frnk/capabilities/analytics-impl/CLAUDE.md`.

## 7. Inherit build configuration (single source of truth)

The host **does not** redeclare SDK targets or library versions — it inherits them from frnk.

**Version catalog.** A composite build does not auto-share `gradle/libs.versions.toml`, so import it
explicitly under a distinct name:

```kotlin
// host settings.gradle.kts
dependencyResolutionManagement {
    versionCatalogs {
        create("frnkLibs") { from(files("frnk/gradle/libs.versions.toml")) }
    }
}
```

Now the host references the same pinned versions frnk uses — e.g. `implementation(frnkLibs.koin.core)`,
`alias(frnkLibs.plugins.kotlin.multiplatform)`.

**SDK targets.** `min/compile/targetSdk` are the **single source of truth** in
`frnk/gradle/libs.versions.toml` (`android-minSdk` / `android-compileSdk` / `android-targetSdk`) — read
by frnk's own modules and inherited by hosts via the catalog:

```kotlin
// host android block
compileSdk = frnkLibs.versions.android.compileSdk.get().toInt()
minSdk     = frnkLibs.versions.android.minSdk.get().toInt()
```

**Convention plugin (optional).** frnk's standard KMP modules apply the `frnk.kmp.library` convention
plugin from its `build-logic` included build (jvmToolchain 17 + Android SDK + bare iOS targets in one line).
A host that adds its own KMP library modules can apply the same plugin by adding
`includeBuild("frnk/build-logic")` to its `pluginManagement` and `plugins { id("frnk.kmp.library") }`.

## 8. Spin up the whole app with `FrnkApp`

> **Shortcut for the common shape.** If your app is the standard `onboarding → tab shell
> (Home · <custom> · Settings) → paywall`, use **`frnkTabbedRootModule(customTab) { … }`** (`:ui-app`) for
> `FrnkApp`'s `onNavigationModule` instead of hand-writing the root + nested Koin modules. You declare only
> the content slots — `home { nav -> }`, `custom { route, nav -> }`, `settings { nav -> }` (required) +
> optional `onboarding { onComplete -> }` / `paywall { onClose -> }`; the helper wires the `navigation<…>`
> registration, the cross-level navigation (the slots get a `FrnkTabNavigator` with `open`/`back`/
> `openPaywall`/`showOnboarding`), and the onboarding→Tab / paywall→back conventions. Pair it with
> `startRoute = rememberFrnkRootStartRoute()` for first-launch onboarding gating. See `:demo-shared`'s
> `FrnkDemoApp` for the reference call. The rest of this section documents the lower-level `FrnkApp` path for
> full control.

After `initializeFrnk(...)` (§4), **`FrnkApp`** (`:ui-app`) is the app root. It owns only the app chrome —
`FrnkTheme` + the `AppearanceController`-driven light/dark + system-bar appearance + a single root
`NavDisplay` over `FrnkRootRoute` (seeded at `Onboarding`) — and hands the navigation graph to you. You
supply two lambdas: `onSavedStateConfiguration` (the root saved-state config, normally `frnkRootNavConfig()`)
and `onNavigationModule(backStack)`, which returns a Koin `navigation<Route> { … }` module registering your
root destinations (it's loaded via `loadKoinModules`). Brand the whole app by passing a `themeConfig`
(`FrnkThemeConfig`, §2) — it flows into the `FrnkTheme` `FrnkApp` owns; omit it for the bundled palette:

```kotlin
setContent {
    FrnkApp(
        onSavedStateConfiguration = { frnkRootNavConfig() },
        themeConfig = FrnkThemeConfig(lightColorOverrides = mapOf(colorPrimary to Brand.Primary)),
        onNavigationModule = { backStack -> myRootNavigationModule(backStack) },
    )
}
```

The root module registers the `FrnkRootRoute` destinations (`Onboarding` / `Tab` / `Paywall`) and mounts the
tabbed surface — **`FrnkNestedNavScaffold`** — at the `Tab` destination. Nothing is auto-mounted: you wire the
paywall, onboarding, and tab navigation yourself:

```kotlin
fun myRootNavigationModule(backStack: NavBackStack<NavKey>) = module {
    navigation<FrnkRootRoute.Onboarding> {
        MyOnboardingScreen(onDone = { backStack.clearAndNavigateTo(FrnkRootRoute.Tab) })
    }

    navigation<FrnkRootRoute.Tab> {
        FrnkNestedNavScaffold(
            onSavedStateConfiguration = { frnkNestedNavConfig(myHostRoutes) },
            onNestedNavigationModule = { nestedBackStack -> myNestedModule(nestedBackStack) },
        )
    }

    navigation<FrnkRootRoute.Paywall> {
        FrnkPaywallDestination(features = listOf("Unlimited everything", "No ads"), onClose = { backStack.back() })
    }
}
```

- **`FrnkNestedNavScaffold(onSavedStateConfiguration, onNestedNavigationModule)`** is a **fixed three-tab**
  (`Home · Components · Settings`) multiple-back-stack tabbed scaffold. The bar items (labels, theme icon
  tokens, SF-Symbols, and the routes `FrnkTabRoute.Home` / `FrnkTabRoute.Custom("Components")` /
  `FrnkTabRoute.Settings`) are defined **inside** the scaffold; you supply only the saved-state config and a
  nested navigation module that registers the destinations behind those three routes. The scaffold owns the
  `FrnkNavDisplay` + the persistent adaptive bottom bar, and reserves the bottom inset via
  `LocalFrnkBottomBarInset`. Selection lives in the MVI `FrnkNestedNavViewModel` (registered by
  `frnkNestedNavModule`, which `frnkUiModules()` carries), not in `remember`. **Interim:** a single shared
  back stack currently drives every tab; per-tab back stacks and the back-from-a-non-home-tab-root → home
  convention are a planned follow-up.
- Drive navigation through the MVI effect channel: a ViewModel emits a navigation `UiEffect`, a single
  effect collector (e.g. `FrnkScreen`'s `onEffect`) mutates the host-owned `NavBackStack`
  (`backStack.navigateTo` / `back` / `clearAndNavigateTo`) — collect it in exactly one place (single-consumer channel).
- The **batteries are yours to wire** — paywall (`FrnkPaywallDestination` from `:shared-monetization-ui`),
  onboarding, and the entitlement-driven Settings are registered by your navigation module, not auto-mounted.
- `:demo-shared`'s `FrnkDemoApp` is the reference integration — the single shared composable both
  `demo-android` and `iosDemoApp` call. Its `RootNavigationModule` (root) + `NestedNavigationModule` (tabs)
  are the canonical example of this shape: a Home / Components / Settings tabbed surface, with the demo wiring
  its own paywall and onboarding.

### 8.1 Bottom-nav icons — fixed, theme-token driven, no host asset step

`FrnkNestedNavScaffold`'s three items are fixed and defined inside the scaffold — `Home`
(`FrnkIconSource.Token(iconNavHome)`, SF-Symbol `"house"`), `Components`
(`FrnkIconSource.Token(iconNavComponent)`, SF-Symbol `"square.grid.2x2"`), and `Settings`
(`FrnkIconSource.Token(iconNavSettings)`, SF-Symbol `"gearshape"`) — so there is **nothing for the host to
declare** here. On **Android** the bar is a Material3 Expressive `HorizontalFloatingToolbar` that resolves each
`FrnkIconSource.Token` to an `ImageVector` and renders it directly — it never touches `DrawableResource`, so
**there is no host-side asset-bundling step** (the old `MissingResourceException` / `assets/composeResources/…`
workaround is gone). iOS-only: the library's older-iOS Compose fallback needs a `DrawableResource`, which the
toolkit supplies internally via a single bundled placeholder — again nothing for the host to do.

## 9. Component style guide — the component `*State` taxonomy

Every frnk UI component takes an `@Immutable *State` value (callbacks are separate params, before
`modifier`). The *shape* of that state falls into exactly one of **three sanctioned categories** — pick by
what the component actually does, and don't force a component into the wrong one:

**Category A — Stateful (the default).** A `sealed interface` with a `Content` data class and a
`data object Skeleton` (plus a `data class Error` where there's a real error visual). Use it whenever the
component toggles between runtime visual states (loading vs loaded). The composable `when`-switches; the
`Skeleton` branch is a **non-interactive** token-driven placeholder. Copy this shape for new components
(host or toolkit):

```kotlin
sealed interface FrnkButtonState {
    @Immutable
    data class Content(
        val text: String,
        val enabled: Boolean = true,
        val variant: FrnkButtonVariant = FrnkButtonVariant.Filled,
    ) : FrnkButtonState

    data object Skeleton : FrnkButtonState
    // data class Error(val message: String) : FrnkButtonState   // when the component shows errors
}

@Composable
fun FrnkButton(state: FrnkButtonState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        is FrnkButtonState.Content -> { /* interactive button, styled from Theme[...] tokens */ }
        FrnkButtonState.Skeleton  ->
            Box(modifier.frnkSkeleton(FrnkSkeleton(enabled = true), shape = shapeButton)) // inert placeholder
    }
}
```

Members today: `FrnkButton`, `FrnkSwitch`, `FrnkSegmentedControl`, `FrnkIcon`, `FrnkIconButton`,
`FrnkListRow`, `FrnkLabeledValue`, `FrnkProfileHeader`.

**Category B — Variant (shared-field).** A `sealed class` whose mutually-exclusive variants share *stored*
`open val` styling fields. This is the **one** case a sealed *class* is correct rather than an interface: a
sealed interface can't carry stored properties, so variants that share fields need the class. It may still add
a `Skeleton` object if the component loads. Members: `FrnkDividerState` (`Horizontal`/`Vertical`; no skeleton
— a divider is always-on chrome) and `FrnkTextState` (semantic text variants that share styling fields, with
a per-subtype `skeleton` field **and** a `Skeleton` object).

**Category C — Single-state.** A plain `@Immutable data class`, no `Skeleton`. Use it when the component has
exactly one visual state, is terminal, is interaction-only chrome, or delegates its skeleton to child states
— and say which in a doc comment. Members: `FrnkTopAppBarState` (single visual state; search is a field),
`FrnkEmptyStateState` (terminal zero-content — you skeletonize the *eventual* content, never the empty
state), `FrnkSwipeableState` (interaction chrome; the wrapped content owns its skeleton), `FrnkListSectionState`
(skeleton carried by the child `FrnkListRowState` rows).

**Ergonomic secondary constructors (cross-cutting).** A `Content` (or a Category-B variant) may add a
secondary constructor that wraps a raw Compose type in the toolkit's source wrapper so the common call site
stays terse — `FrnkIconState.Content(imageVector)` → `FrnkIconSource.Vector`,
`FrnkIconButtonState.Content(imageVector)` → `FrnkIconSource.Vector`, `FrnkTextState.<variant>(text: String)`
→ `FrnkStringSource.Raw`. The **primary** constructor stays the source-wrapper form; add the secondary only
when the wrapped type is the overwhelmingly common case (it deliberately doubles a small slice of the surface
to keep call sites clean).

Rules across all categories: state is hoisted into the feature's `MviViewModel` (never
`remember { mutableStateOf }` for screen/business state); styling comes from
`Theme[colors|textStyles|shapes|spacing|iconSizes][token]`, never hardcoded `Color(0xFF…)` / raw `.dp`; any
`Skeleton` branch renders no clickable/toggleable node. Each outlier `*State` (Categories B and C) carries a
one-line `State shape — **Category X**` marker at its declaration. See `frnk/ui/components/CLAUDE.md` for the
full per-component convention.

## 10. Demo isolation & the upstream rule

`demo-android` / `iosDemoApp` are **pure host harnesses**: they only initialize frnk, provide config
overrides, and showcase default behavior — they contain **zero** reusable library logic (the shared demo UI
itself lives in `:demo-shared`, consumed by both). Treat them as disposable references for how a host wires
the toolkit.

**The upstream rule:** anything reusable belongs **inside** a `frnk` module, never at the app layer. When
building a feature in a host, ask whether it can be abstracted upstream into the toolkit (a new atom, a
backend interface + impl pair, a scaffold) so every future host inherits it. If something genuinely must live
at the app level (a host-only screen, brand assets), keep it there — but the default is upstream.
