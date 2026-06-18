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
| `:core-di` | `core-di` | `projects.coreDi` | Bootstrap: `initializeFrnk(modules)` + `requireFrnkKoin()`. |
| `:shared-utils` | `shared-utils` | `projects.sharedUtils` | Root utils: coroutines, datetime, `AppResult`, `PlatformInfo`, `Frnk.VERSION`. |
| `:core-mvi` | `core-mvi` | `projects.coreMvi` | MVI engine (`MviViewModel`, `UiText`); no Compose. |
| `:core-nav` | `core-nav` | `projects.coreNav` | Navigation3 contract (`ToolkitRoute`, back-stack helpers); no Compose. |
| `:haptics` | `haptics` | `projects.haptics` | `HapticFeedback`/`HapticType` contract + multihaptic engine. |
| `:ui-theme` | `ui-theme` | `projects.uiTheme` | `FrnkTheme` + tokens (compose-unstyled). |
| `:ui-components` | `ui-components` | `projects.uiComponents` | `Frnk*` atoms / molecules / organisms. |
| `:ui-scaffolds` | `ui-scaffolds` | `projects.uiScaffolds` | Page templates + Compose MVI/nav bindings. |
| `:ui-bottom-nav` | `ui-bottom-nav` | `projects.uiBottomNav` | Adaptive bottom nav + `FrnkTabbedNavScaffold` (the one-call tabbed app). **Sole Material3 module.** |
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

`FrnkAppScaffold` / `FrnkTabbedNavScaffold` take the theme override as `config.theme` (a `FrnkThemeConfig`)
and wrap `FrnkTheme` for you.

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

## 3. Map ToolkitRoute to Compose screens

The toolkit's `ToolkitRoute` (`:core-nav`) is a `@Serializable sealed interface … : NavKey` of
default routes (`Home`, `Settings`, `Onboarding`, `Paywall`). When using `FrnkTabbedNavScaffold` /
`FrnkAppScaffold` (§8), those routes are already wired — you don't map them. For a hand-wired nav3
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

After bootstrap, `FrnkAppScaffold(config = FrnkAppConfig(...)) { /* home items */ }` (§8) is the
batteries-included app root; it fails fast with an explanation if `initializeFrnk` didn't run.

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

## 8. Spin up the whole app with `FrnkAppScaffold`

After `initializeFrnk(...)` (§4), the **batteries-included app root** stands up a complete tabbed app —
theme, type-safe nav3 with per-tab back stacks, the adaptive bottom bar (the fixed `Home · feature ·
Settings` three-tab shape), a Home template you fill with content, the default Settings catalogue driven
by the live `EntitlementManager` (Upgrade → paywall, Restore, Manage Subscription, appearance, feedback),
an optional onboarding flow, and the auto-mounted paywall — in one call:

The host's declarative input is one `@Immutable` **`FrnkAppConfig`** bundle, grouped into a sub-config
per feature area (`*Config` = host input declared once; the toolkit's runtime state stays in `*State` /
`*ViewState`). The composable keeps only *behaviour* as parameters — the `@Composable` slots
(`homeContent`/`entries`/`effects`), the event callbacks (`onMessage`/`onHomeEffect`), and the runtime
controllers (`appearanceController`/`pendingRoutes`):

```kotlin
setContent {
    val config = remember {
        FrnkAppConfig(
            app = FrnkAppInfo(name = "MyApp", version = "v1.0.0"),
            nav = FrnkNavConfig(
                feature = FrnkFeatureItem(                     // the bar's one host-configurable tab
                    route = SessionsRoute,                     //   register it in `entries` below
                    label = "Sessions",
                    icon = Lucide.CalendarClock,               //   ImageVector (Android)
                    iosSystemIcon = "calendar",                //   SF-Symbol (iOS)
                ),
                hostRoutes = SerializersModule { /* your @Serializable routes */ },
            ),
            theme = myThemeConfig(),                           // §2 token overrides
            settings = FrnkSettingsConfig(
                extraSections = listOf(myPrefsSection),        // injected before Legal by default
            ),
            onboarding = FrnkOnboardingConfig(
                pages = myOnboardingPages,                     // empty → no onboarding entry
                showOnFirstLaunch = true,                      // auto-present once on first launch (default)
            ),
            monetization = FrnkMonetizationConfig(
                paywallFeatures = listOf("Unlimited everything", "No ads"),
            ),
        )
    }
    FrnkAppScaffold(
        config = config,
        onHomeEffect = { effect -> /* HomeEffect.ActionInvoked(key) / NavigationInvoked */ },
        entries = { scope -> entry<SessionsRoute> { … } },     // the feature tab's root + your pushes
        effects = { scope -> EffectCollector(vm.effects) { scope.navigateTo(it.route) } },
    ) {
        // Home tab body — the scaffold owns the scrolling column + bar insets.
        MyHomeCards()
    }
}
```

- The bar always shows exactly three tabs — `Home · feature · Settings`. The center **`feature`** tab is
  the only one you configure (`FrnkFeatureItem`); it's a real navigable tab (own back stack,
  re-tap-to-root). Point it at your app's signature surface (a "New X" flow, a capture screen, the main
  library) and **register its `route` in `entries`** — the shell owns only Home/Settings/Onboarding.
- Every extension point receives a **`FrnkAppScope`** (`navigateTo` / `back` / `clearAndNavigateTo`) so a
  single `EffectCollector` drives navigation.
- Don't re-register the built-in routes (`ToolkitRoute.Home`/`Settings`/`Onboarding`/`Paywall`) in
  `entries` — nav3 throws on duplicate entry registrations.
- When `onboarding.pages` is supplied, onboarding is **auto-presented once on first launch** and
  persisted through a `KeyValueStore`-backed gate (install `prefsModule` for cross-launch persistence;
  with no store bound it falls back to once-per-session). Set `onboarding.showOnFirstLaunch = false` to
  present it only on demand (Settings → Show onboarding). Full-window screens (onboarding, paywall) use
  **`FrnkFullScreenScaffold`** — an immersive template with an always-on ✕ that reserves the safe-area
  insets + close-button band for you; reuse it for your own full-screen surfaces.
- `FrnkAppScaffold` (`:ui-app`) layers the monetization batteries over **`FrnkTabbedNavScaffold`**
  (`:ui-bottom-nav`). `:demo-shared`'s `FrnkDemoApp` is the reference integration of `FrnkAppScaffold` —
  the single shared composable both `demo-android` and `iosDemoApp` call. A module that genuinely can't
  depend on `:ui-app` (e.g. one that must avoid Material3, or a custom tab shape) composes
  `FrnkTabbedNavScaffold` directly — the same one-call tabbed app minus the monetization batteries
  (auto paywall + first-launch onboarding + live entitlement Settings), wiring those itself.

### 8.1 Bottom-nav icons — `ImageVector`, no host asset step

The adaptive bottom bar (`FrnkBottomFloatingBar`, `:ui-bottom-nav`) takes **`ImageVector`** icons in the common
API (`FrnkNavBarItem.icon` / `FrnkBottomNavTab.icon` / `FrnkFeatureItem.icon`), plus an `iosSystemIcon`
SF-Symbol string for the native iOS bar. On **Android** the bar is a Material3 Expressive
`HorizontalFloatingToolbar` that renders the `ImageVector` directly — it never touches `DrawableResource`, so
**there is no host-side asset-bundling step** (the old `MissingResourceException` /
`assets/composeResources/…` workaround is gone).

Supply the `feature` tab's icon as a plain `ImageVector` (e.g. a Lucide vector, or your own), the same way the
demo's "Components" tab uses `Lucide.Component`. Defaults for the Home/Settings bookends come from theme icon
tokens (`iconNavHome` / `iconSettings`), overridable via `FrnkThemeConfig.iconOverrides`. iOS-only: the
library's older-iOS Compose fallback needs a `DrawableResource`, which the toolkit supplies internally via a
single bundled placeholder — nothing for the host to do.

## 9. Component style guide — sealed state + `Skeleton` object

**Every frnk UI component with multiple visual states models its state as a `sealed interface` with a
`Content` data class and a `data object Skeleton`** (and an `Error` data class where there's a real error
visual). The composable `when`-switches; the `Skeleton` branch is a **non-interactive** token-driven
placeholder. Copy this shape for new components (host or toolkit):

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

Rules: state is hoisted into the feature's `MviViewModel` (never `remember { mutableStateOf }` for
screen/business state); styling comes from `Theme[colors|textStyles|shapes|spacing|iconSizes][token]`, never
hardcoded `Color(0xFF…)` / raw `.dp`; the `Skeleton` branch renders no clickable/toggleable node.
Single-state or terminal components (a pure divider, a terminal empty state, interaction-only chrome) stay
non-sealed and skip the `Skeleton` object — note why. `FrnkText` additionally keeps a per-subtype `skeleton`
field for content-sized text skeletons. See `frnk/ui/components/CLAUDE.md` for the full convention.

## 10. Demo isolation & the upstream rule

`demo-android` / `iosDemoApp` are **pure host harnesses**: they only initialize frnk, provide config
overrides, and showcase default behavior — they contain **zero** reusable library logic (the shared demo UI
itself lives in `:demo-shared`, consumed by both). Treat them as disposable references for how a host wires
the toolkit.

**The upstream rule:** anything reusable belongs **inside** a `frnk` module, never at the app layer. When
building a feature in a host, ask whether it can be abstracted upstream into the toolkit (a new atom, a
backend interface + impl pair, a scaffold) so every future host inherits it. If something genuinely must live
at the app level (a host-only screen, brand assets), keep it there — but the default is upstream.
