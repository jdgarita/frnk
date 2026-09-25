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
| `:core-platform` | `core-platform` | `projects.corePlatform` | SDK-free camera, image, settings, and maps host-service contracts. |
| `:haptics` | `haptics` | `projects.haptics` | `HapticFeedback`/`HapticType` contract + multihaptic engine. |
| `:ui-theme` | `ui-theme` | `projects.uiTheme` | `FrnkTheme` + tokens (compose-unstyled). |
| `:ui-components` | `ui-components` | `projects.uiComponents` | `Frnk*` atoms / molecules / organisms. |
| `:ui-scaffolds` | `ui-scaffolds` | `projects.uiScaffolds` | Page templates + Compose MVI/nav bindings. |
| `:ui-bottom-nav` | `ui-bottom-nav` | `projects.uiBottomNav` | Adaptive bottom nav + `FrnkNestedNavScaffold` (the multiple-back-stack tabbed scaffold). **Sole Material3 module.** |
| `:ui-app` | `ui-app` | `projects.uiApp` | `FrnkApp` + `frnkUiModules()` + `frnkModules { }` builder + `Koin::validateFrnkBootstrap`/`checkFrnkModules()` (§4.1). The app-root apex. |
| `:data-db-api` | `data-db-api` | `projects.dataDbApi` | Room `DatabaseFactory` seam + `databaseSingle` (toolkit owns no schema). |
| `:data-db-impl` | `data-db-impl` | `projects.dataDbImpl` | Platform locations + bundled SQLite driver → `databaseModule`. |
| `:data-prefs-api` | `data-prefs-api` | `projects.dataPrefsApi` | `KeyValueStore` + typed `Preference<T>`. |
| `:data-prefs-impl` | `data-prefs-impl` | `projects.dataPrefsImpl` | multiplatform-settings → `prefsModule`. |
| `:analytics-api` | `analytics-api` | `projects.analyticsApi` | `AnalyticsTracker`/`CrashReporter` contracts (no no-op). |
| `:analytics-posthog` | `analytics-posthog` | `projects.analyticsPosthog` | PostHog `AnalyticsTracker` → `postHogAnalyticsModule(config)`. Mandatory; `:ui-app` carries it. |
| `:crash-sentry` | `crash-sentry` | `projects.crashSentry` | Sentry `CrashReporter` → `sentryCrashReportingModule(config)`. Mandatory; `:ui-app` carries it. |
| `:identity-api` | `identity-api` | `projects.identityApi` | SDK-free `AnonymousIdentityProvider` contract (bound by `revenueCatIdentityModule`, `:monetization-impl`). |
| `:camera` | `camera` | `projects.camera` | api-only no-op scaffold → `cameraModule` (no impl yet). |
| `:permissions` | `permissions` | `projects.permissions` | api-only no-op scaffold → `permissionsModule` (no impl yet). |
| `:monetization-api` | `monetization-api` | `projects.monetizationApi` | `EntitlementManager`/`FeatureGate` → `monetizationModule`. |
| `:monetization-impl` | `monetization-impl` | `projects.monetizationImpl` | RevenueCat `EntitlementProvider` → `revenueCatModule`. |
| `:shared-monetization-ui` | `shared-monetization-ui` | `projects.sharedMonetizationUi` | Paywall UI + `paywallScaffoldModule` + `rememberFrnkSettingsHandler`. |

(`:demo-shared` / `:demo-android` and the `iosDemoApp` Xcode target are internal smoke harnesses —
never host-consumable.)

## 1. Bring your own Room schema

The toolkit owns the **database factory** (`DatabaseFactory`, `:data-db-api`, bound by
`databaseModule` from `:data-db-impl`), never a schema. Your entities, DAOs and `@Database` class
live in a module of yours, which applies the Room and KSP Gradle plugins — the toolkit cannot do
that for you (its convention plugins are not consumable through the composite build), so the
schema-owning module's build script looks like this:

```kotlin
plugins {
    // … your KMP + Android plugins …
    alias(frnkLibs.plugins.androidx.room)
    alias(frnkLibs.plugins.ksp)
}

kotlin.sourceSets.commonMain.dependencies {
    api("dev.jdgarita.frnk:data-db-api")          // RoomDatabase + DatabaseFactory + databaseSingle
}

dependencies {
    add("kspAndroid", frnkLibs.androidx.room.compiler)
    add("kspIosArm64", frnkLibs.androidx.room.compiler)
    add("kspIosSimulatorArm64", frnkLibs.androidx.room.compiler)
}

room { schemaDirectory("$projectDir/schemas") }   // commit the exported schemas; review every bump

// Only if the module has `withHostTest {}` AND you run `check`/`lint` on it: AGP's host-test lint
// reads the KSP-generated dirs without depending on the task that writes them, and Gradle's task
// validation fails the build. (`testAndroidHostTest` alone never trips it.)
tasks
    .matching { it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest" }
    .configureEach { dependsOn("kspAndroidHostTest") }
```

The database class is ordinary Room KMP — `@ConstructedBy` plus the `expect object` constructor
KSP fills in per target:

```kotlin
@Database(entities = [MyEntity::class], version = 1, exportSchema = true)
@ConstructedBy(MyDbConstructor::class)
abstract class MyDb : RoomDatabase() {
    abstract fun myDao(): MyDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MyDbConstructor : RoomDatabaseConstructor<MyDb> {
    override fun initialize(): MyDb
}
```

In your host app's DI graph, use the `databaseSingle` Koin helper (`:data-db-api`) — it resolves
the `DatabaseFactory`, opens the file at the platform location, applies the toolkit defaults (the
bundled SQLite driver, `Dispatchers.Default` as the query context) and registers the built
database as a `single<MyDb>`:

```kotlin
val hostDatabaseModule = module {
    databaseSingle<MyDb>("host.db")
    single<MyRepository> { RoomMyRepository(get<MyDb>().myDao()) }
}
```

(`demo/shared`'s `demoNotesModule` + `DemoDatabase` is the worked example.) The trailing lambda
adjusts the builder after the defaults — callbacks, a different query context, Room's own
`fallbackToDestructiveMigration` — and the raw long form stays valid for anything the helper
doesn't cover:

```kotlin
single<MyDb> { get<DatabaseFactory>().open<MyDb>("host.db") { setQueryCoroutineContext(Dispatchers.IO) } }
```

**Where the file lives.** Android: `context.getDatabasePath(name)` (Room's own default). iOS:
`<Application Support>/<name>`. Both are what the blueprint host has shipped since its first
release, so a host moving onto the seam finds its existing data.

**Wipe-on-version-bump (pre-launch alternative to writing migrations).** Pass a `SchemaUpgrade` to drop
and recreate the database when your schema generation changes — no migration files:

```kotlin
databaseSingle<MyDb>("host.db", SchemaUpgrade.WipeOnVersionBump(4))
```

`version` is *your* schema generation counter (bump it on any schema-shape change; independent of the
`@Database(version = …)` Room tracks). When the value persisted for that db name differs from `version`
**and** a file already exists, the factory deletes the file (+ `-wal`/`-shm`) before Room opens it, then
records the new version. The factory persists the version through the host's `KeyValueStore`, so
**`WipeOnVersionBump` requires `prefsModule`** in the graph (it throws otherwise). The default
`SchemaUpgrade.None` opens the file as-is and leaves migrations to Room.

On Android, before `startKoin { ... }`, point the toolkit at your `Application` context
(`DatabaseContext` lives in `:core-di`, package `dev.jdgarita.frnk.di` — Room's builder and the
SharedPreferences-backed `KeyValueStore` both resolve through it):

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

**Language.** The string axis is language-aware: the toolkit ships default catalogs per `FrnkLanguage`
(English + Spanish today), and `FrnkTheme` picks the catalog from `FrnkThemeConfig.language` — or, when
that is `null` (the default), from the device language (`systemFrnkLanguage()`). Your `stringOverrides`
are applied on top and always win per token, so override values should already be in the language you
want shown. A host with an in-app language selector persists its choice and passes
`FrnkThemeConfig(language = …)`; the theme re-reads the config every composition, so switching re-renders
all toolkit copy immediately. A missing translation in a non-English catalog falls back to English per
token, never to a blank. Billing errors surface through the same axis: `PaywallViewModel` maps
`MonetizationError` to the `stringError*` tokens (`MonetizationError.toStringSource()`), so never render
`AppError.message` directly — it is the enum's English diagnostic. RevenueCat hosts localize the offline
paywall fallback via `RevenueCatConfig(paywallFallback = ProMetadata(…), savingsBadgeTemplate = "…%1$d…")`
— that copy is the host's, not the toolkit's.

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
            databaseModule,                      // :data-db-impl — Room DatabaseFactory (bring your own schema, §1)
            prefsModule,                         // :data-prefs-impl — KeyValueStore (multiplatform-settings)
            postHogAnalyticsModule(PostHogAnalyticsConfig(environment = env)),                // AnalyticsTracker — MANDATORY; key ships in frnk
            sentryCrashReportingModule(SentryCrashReportingConfig(dsn = BuildConfig.SENTRY_DSN, environment = env)), // CrashReporter — MANDATORY; YOUR Sentry project
            // Monetization stack (optional — omit all three to run without entitlements):
            revenueCatModule,                    // :monetization-impl — EntitlementProvider
            revenueCatIdentityModule,            // :monetization-impl — AnonymousIdentityProvider (or your own binding)
            monetizationModule,                  // :monetization-api — EntitlementManager/FeatureGate
            paywallScaffoldModule,               // :shared-monetization-ui — paywall VM
        ) + hostModules,                         // your repositories, feature VMs, schema module — after the toolkit's
) {
    // extraConfig: Koin DSL escape hatch (logging, overrides).
    // allowOverride(true)  // lets a host module override a toolkit binding (e.g. a custom EntitlementProvider)
}
```

- **Host modules** go in the same list, **after** the toolkit's — so with `allowOverride(true)` a
  host can override a toolkit binding (a custom `DatabaseFactory`, a custom `EntitlementProvider`).
  The one exception is the observability pair: `frnkModules { }` installs PostHog + Sentry **last**,
  after your `modules(...)`, so a stray `AnalyticsTracker`/`CrashReporter` in a host module can never
  shadow them; on the raw list, keep the two provider modules last yourself.
- The Android overload also sets `DatabaseContext.application` and registers `androidContext(...)`,
  so the §1 context line is only needed if you bypass `initializeFrnk`.
- **Monetization opt-out:** don't pass the three monetization modules. A host using a different
  provider passes its own `EntitlementProvider` (optionally with the toolkit's `monetizationModule` /
  `paywallScaffoldModule` over it).
- **Web purchases (RevenueCat Web Billing + Redemption Links).** A purchase made on the web reaches
  the phone as a one-time deep link, `rc-<rc-app-id>://redeem_web_purchase?redemption_token=…`
  (each RevenueCat *app* has its own scheme — copy it from the dashboard). The toolkit owns the
  redemption (`EntitlementManager.redeemWebPurchase(url)` → `AppResult<Boolean,
  WebPurchaseRedemptionError>`, `isPro` updated on success, `web_purchase_redeemed{result}` tracked)
  but **not** the plumbing: the host registers the scheme (an `android.intent.action.VIEW` +
  `BROWSABLE` intent filter on the launcher Activity with `launchMode="singleTop"`, forwarding
  `intent.data` from both `onCreate` and `onNewIntent`; `CFBundleURLTypes` in `Info.plist` plus
  SwiftUI's `.onOpenURL`) and hands the URL string through. Gate the call on `SyncAuthUseCase.identify()`
  first, exactly like a restore — the purchase attaches to whatever app user is current, and an
  offline launch may have left RevenueCat on its transient anonymous id. `Expired` carries the
  obfuscated address RevenueCat re-mailed a fresh link to; `NotARedemptionLink` means the URL was
  something else and is not tracked, so it is safe to route every incoming link through.
- **Observability is not optional.** Every host installs `postHogAnalyticsModule(PostHogAnalyticsConfig(environment = …))`
  and `sentryCrashReportingModule(SentryCrashReportingConfig(dsn = …, environment = …))` — the toolkit's only
  `AnalyticsTracker` / `CrashReporter` and there is no no-op; `frnkModules { observability(sentry = …) }` (§4.1)
  does it for you. **The host supplies only its Sentry DSN** (each app has its own Sentry project); the PostHog
  key is the toolkit's — every frnk app reports to one PostHog project, whose key ships in `:analytics-posthog`
  (`FrnkPostHogProject.API_KEY`, the default `PostHogAnalyticsConfig.apiKey`, generated at build time from
  `POSTHOG_API_KEY` in the **frnk checkout's** gitignored `local.properties` — never committed; see §7). A blank DSN throws at config
  construction, so wire it from your build config exactly as described in
  [Supplying per-app keys — the standard](#supplying-per-app-keys--the-standard). Never bind your
  own `AnalyticsTracker`/`CrashReporter`; inject the toolkit's (`koinInject<AnalyticsTracker>()`) for your
  app's own `trackCustom`/`screen`/`recordException`. There is no remote-config capability (retired 2026-09-22 — a host that needs one owns it outside frnk). `:camera` / `:permissions` are
  api-only scaffolds — install `cameraModule` / `permissionsModule` for their no-op defaults until a
  real impl ships.

**Identity is one slot, one binding.** `revenueCatIdentityModule` binds `AnonymousIdentityProvider`
over the RevenueCat app user id: a local read, no network, minted the moment the host calls
`Purchases.configure(...)` and persisted by the SDK across launches — the natural choice for an
accountless host on RevenueCat. The toolkit never calls `Purchases.logOut()`, so an install that once
identified RevenueCat with another id keeps it. It is the toolkit's only identity binding (the
Firebase-backed `firebaseIdentityModule` was retired on 2026-09-22); a host with its own account
system binds its own `AnonymousIdentityProvider` instead. Install exactly one (`frnkModules { identity
= … }` makes a second one unrepresentable; on the raw list, two would silently shadow each other). The API module contains no
SDK types, and no longer exposes a signed token — a backend credential is the host's concern.

**Propagating the identity.** `AnonymousIdentityProvider` only *produces* a uid. Everything that
*consumes* one — `AnalyticsTracker`, `CrashReporter`, `EntitlementProvider`, `EntitlementManager` —
implements `IdentitySource` (`suspend fun identify(id: String)`), and `SyncAuthUseCase` is what fans
the uid out to all of them. Call it once at bootstrap (best-effort, ignore the result) and again
wherever you must not proceed with an unsynced identity (gate on the `AppResult`). Crash reports then
carry the uid automatically, and Analytics gets it in the reserved User-ID field.

A telemetry sink failing **never** fails the sync — only the billing backend does — so an
unconfigured or unreachable sink degrades telemetry rather than blocking your app. Anonymous credentials normally persist across launches but are not recoverable after
uninstall or cleared app data unless the host later links the user to a durable account.

**iOS** (on launch, via a Kotlin bootstrap function your umbrella shared module exposes to Swift —
the common `initializeFrnk(modules)` overload, no context param):

```kotlin
fun bootstrapMyAppKit(): KoinApplication =
    initializeFrnk(modules = frnkUiModules() + databaseModule + prefsModule + /* … */ myAppModules)
```

After bootstrap, `FrnkApp(onSavedStateConfiguration, onNavigationModule)` (§8) is the app root; it fails
fast with an explanation if `initializeFrnk` didn't run.

### Supplying per-app keys — the standard

> **Claude: every frnk host follows this layout, Faint included. When scaffolding a new app, set it up
> exactly like this — no key constants in Kotlin or Swift, no placeholders in code.**

Which keys are whose:

| Key | Owner | Where it lives |
| --- | --- | --- |
| PostHog project API key | **the toolkit** — one PostHog project for every frnk app | `FrnkPostHogProject.API_KEY` (`:analytics-posthog`), **generated at build time** from `POSTHOG_API_KEY` in the frnk checkout's gitignored `local.properties` (`frnk/local.properties` in a host — copy `frnk/local.properties.example`; `-PPOSTHOG_API_KEY=` / the env var also work, e.g. on CI). The build fails without it. Never in host code, never committed. Apps are told apart by the SDK's `$app_namespace`/`$app_name`, debug vs release by the `environment` super property. |
| Sentry DSN | **each app** (its own Sentry project) | the host's build config, below |
| RevenueCat public SDK keys (`goog_…` / `appl_…` / `test_…`) | **each app, per platform** | the host's build config, below |

**Android** — the gitignored `local.properties` feeds `BuildConfig` (track a `local.properties.example`):

```kotlin
// app/build.gradle.kts
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
android.defaultConfig {
    buildConfigField("String", "SENTRY_DSN", "\"${localProperties.getProperty("SENTRY_DSN", "")}\"")
    buildConfigField("String", "REVENUECAT_API_KEY", "\"${localProperties.getProperty("REVENUECAT_ANDROID_API_KEY", "")}\"")
}
// Application.onCreate: SentryCrashReportingConfig(dsn = BuildConfig.SENTRY_DSN, environment = if (BuildConfig.DEBUG) "debug" else "release")
```

**iOS** — an xcconfig pair, forwarded through `Info.plist`, read by the Kotlin bootstrap:

```
<app>/Configuration/Config.xcconfig            tracked; the app target's base configuration (Debug + Release)
<app>/Configuration/Secrets.xcconfig           gitignored; the real values
<app>/Configuration/Secrets.xcconfig.template  tracked; blank values + comments
```

```
// Config.xcconfig — `#include?`, not `#include`: a fresh clone still configures and builds,
// the keys expand empty, and the Kotlin config fails at launch naming the missing one.
#include? "Secrets.xcconfig"
```

```
// Secrets.xcconfig — xcconfig treats "//" as a comment, so URLs split the slashes with "$()":
SENTRY_DSN = https:/$()/<key>@<org>.ingest.sentry.io/<project>
REVENUECAT_API_KEY = appl_…
```

```xml
<!-- Info.plist -->
<key>SENTRY_DSN</key>          <string>$(SENTRY_DSN)</string>
<key>REVENUECAT_API_KEY</key>  <string>$(REVENUECAT_API_KEY)</string>
```

```kotlin
// umbrella module, iosMain — the bootstrap Swift calls; an unexpanded "$(…)" counts as blank
private fun infoPlistValue(key: String): String {
    val raw = (NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String).orEmpty().trim()
    return if (raw.startsWith("$(")) "" else raw
}
fun bootstrapMyAppKit(): KoinApplication =
    initializeFrnk(modules = frnkModules {
        observability(sentry = SentryCrashReportingConfig(dsn = infoPlistValue("SENTRY_DSN"), environment = env))
        // …
    })
```

Set the base configuration in the pbxproj (`baseConfigurationReference = <Config.xcconfig file ref>` on
the target's Debug and Release `XCBuildConfiguration`s, or Xcode → project → Info → Configurations) and
add `Configuration/Secrets.xcconfig` to `.gitignore`. The demo is the worked example of exactly this
layout: `demo/android-app/build.gradle.kts` + `DemoApplication.kt` (Android) and
`demo/ios-app/Configuration/*` + `Info.plist` + `demo/shared`'s `DemoSdks.kt` (iOS).

### 4.1 Optional: the `frnkModules { }` builder + `validate = true` (Tier 2.2)

The explicit list above stays the canonical path. Two **additive** opt-ins (both in `:ui-app`) remove the
two footguns it leaves to host discipline:

```kotlin
initializeFrnk(
    context = this,
    modules = frnkModules {
        observability(sentry = SentryCrashReportingConfig(dsn = BuildConfig.SENTRY_DSN, environment = env))
                                                      // mandatory — build() throws without it; PostHog derives from it
        monetization(provider = revenueCatModule)     // bundles monetizationModule + paywallScaffoldModule
        identity = revenueCatIdentityModule           // single slot; or your own AnonymousIdentityProvider module
        modules(databaseModule, prefsModule, *hostModules.toTypedArray())
    },
    validate = true,
    validator = Koin::validateFrnkBootstrap,
)
```

- **`frnkModules { }`** assembles the list. `observability(sentry = …)` is mandatory and installs the toolkit's
  PostHog + Sentry pair (you never import a provider module or hold a PostHog key): the `postHog` parameter is
  optional and defaults to `PostHogAnalyticsConfig(environment = sentry.environment)` on the toolkit-wide project —
  pass it only to tune a flag (`debug`, `optOut`, …); `identity` is a single slot (unset until you
  choose), so installing two identity bindings — the silent-shadowing footgun — is **unrepresentable**; `monetization(provider)` auto-bundles the trio so you
  can't forget `monetizationModule`/`paywallScaffoldModule`; `frnkUiModules()` is always included. The other impl
  `val`s (`revenueCatModule`, `revenueCatIdentityModule`, …) you still import yourself and assign to a slot.
- **`validate = true` + `validator = Koin::validateFrnkBootstrap`** runs a post-`startKoin` check that throws a
  message naming the exact missing module (one analytics, one crash-reporting, the monetization
  stack the Settings scaffold needs and the `AnonymousIdentityProvider` it reads). This catches the *missing-module* footgun on **either** path — it works with a raw
  `initializeFrnk(modules = …)` list too. Note it runs after start, so it can detect a *missing* module but **not**
  a *duplicate* one (two bindings for one slot collapse to one) — that's what the builder's single slots
  prevent. `KeyValueStore`/`DatabaseFactory` are treated as optional (a local-only host omits them).

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
  (`:monetization-impl`, and the always-present `:analytics-posthog` + `:crash-sentry`) reference
  native iOS SDKs (purchases-ios, posthog-ios, sentry-cocoa) that **your app** supplies via SPM;
  deferred symbol resolution lets the framework link without them.
- Don't add `linkerOpts` for specific frameworks — the consumer keeps full control of the native
  dep list.
- Set `kotlin.disableSwiftPMImport=true` in the host's `gradle.properties`, as frnk's own does.
  Kotlin 2.4 otherwise turns on SwiftPM import for posthog-kmp's declared Swift packages and links
  them itself, which conflicts with the app supplying them — and fails outright on Xcode 27, whose
  linker flags the import's clang wrapper rejects.

The same ownership boundary applies to tests. `:analytics-posthog`, `:crash-sentry`, `:ui-app` and
`:monetization-impl` run their common tests through `testAndroidHostTest`, but skip standalone iOS
simulator test executables: those executables have no consuming Xcode target from which to obtain
PostHog, Sentry or RevenueCat.
Validate their native Apple linkage through an integration host that supplies both packages. The
repository demo is the canonical gate:

```bash
./gradlew :demo-shared:assembleDemoKitDebugXCFramework
xcodebuild build \
  -project demo/ios-app/iosDemoApp.xcodeproj \
  -scheme iosDemoApp \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO
```

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
            // Defer native RevenueCat/Sentry/PostHog symbols to the app's own link step.
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

Keep the **common** surface free of *optional* `*-impl` modules so the framework links no native
cinterop it doesn't need. PostHog + Sentry are the exception by design — `:ui-app` carries them, so
every umbrella framework references posthog-ios + sentry-cocoa and the Xcode project links the
`PostHog` + `Sentry` SPM products. RevenueCat goes **only** in `iosMain` (the demo adds
`monetizationImpl` + `revenuecat.core` there) — the consumer Xcode project then supplies the native
SDK via SPM, and the `dynamic_lookup` flag above lets the framework link locally without it.

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

**2. Upload dSYMs to Sentry** — the last phase; uploads the app dSYM (which, because the framework
is static, already carries frnk's Kotlin frames) so crashes symbolicate. See
[Sentry setup](#sentry-setup-the-crash-reporter) below for the `sentry-cli debug-files upload`
phase and its skip-when-absent guard.

### RevenueCat consumer setup

The toolkit never calls `Purchases.configure(...)`; the consumer app must:

1. Add the **`RevenueCat`** Swift package (`github.com/RevenueCat/purchases-ios`, the 5.x release the
   pinned `purchases-kmp` wraps — 3.7.0 ↔ `from: 5.87.1`; 5.78.0+ is the floor for Xcode 27) via SPM. **Add only the `RevenueCat` product** —
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

### Sentry setup (the crash reporter)

> **Claude: when the user is creating/scaffolding a new iOS app on frnk, walk them through this
> checklist.** The toolkit cannot wire any of this — it's per-app Xcode setup. Skipping step 4 is the
> #1 reason "crashes don't show up in Sentry." (Crashlytics is gone: Sentry is the only crash reporter.)

Your umbrella framework is static, so frnk's Kotlin symbols link into the **app binary**.
`sentryCrashReportingModule` (installed by `frnkModules { observability(…) }`) needs no Swift call and
no native hook of its own: `Sentry.init` runs inside `startKoin` (the binding is `createdAtStart`) and
installs the SDK's unhandled-Kotlin-exception hook on Apple itself. What the app must still do:

1. **Link `sentry-cocoa`** — SPM product `Sentry`, at the version frnk's catalog pairs with
   `sentry-kmp` (the KMP SDK's compat table). The umbrella framework defers the symbols under
   `dynamic_lookup`, so a project that forgets this fails at the app link step, loudly.
2. **Supply the DSN** from the gitignored `Configuration/Secrets.xcconfig` → `Info.plist` value, read
   with `NSBundle` in the Kotlin bootstrap — [the standard](#supplying-per-app-keys--the-standard).
   Blank throws at `SentryCrashReportingConfig` construction — there is no no-op. (No PostHog key to
   supply: it is the toolkit's.)
3. **Confirm Release builds emit dSYMs** — `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym`.
4. **Upload dSYMs** with a run-script phase: `sentry-cli debug-files upload --include-sources
   "$DWARF_DSYM_FOLDER_PATH"`, keyed by `SENTRY_AUTH_TOKEN` / `~/.sentryclirc` on the machine that
   archives (never CI). Declare the dSYM as an input path so Xcode orders the phase after
   `GenerateDSYMFile` under Archive, and skip-with-warning when the token or `sentry-cli` is absent.
5. **Never install another unhandled-exception hook** (CrashKiOS and the like) — two hooks double-report.

KMP specifics: because the umbrella framework is **static**, your app's own dSYM already contains
frnk's Kotlin frames — no separate Kotlin-framework dSYM step. A crash with unsymbolicated Kotlin
frames means no matching dSYM was uploaded — usually a Debug build (`dwarf`, no dSYM) or a missing
run-script. `frnk/capabilities/crash-sentry/CLAUDE.md` has the module's own notes.

## 7. Inherit build configuration (single source of truth)

The host **does not** redeclare SDK targets or library versions — it inherits them from frnk.

**frnk's own `local.properties`.** The submodule keeps its own gitignored `frnk/local.properties`
(copy `frnk/local.properties.example`): `sdk.dir` for its Android build **and `POSTHOG_API_KEY`**, the
toolkit-wide PostHog project key that `:analytics-posthog` compiles into the binaries every host links
(see [Supplying per-app keys](#supplying-per-app-keys--the-standard)). Without it frnk does not build.
On CI, pass `-PPOSTHOG_API_KEY=…` or export the `POSTHOG_API_KEY` env var from a secret instead.

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

**Convention plugins.** frnk's `build-logic` included build publishes its Gradle convention plugins.
A host opts in once, in `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("frnk/build-logic")   // path to the submodule's build-logic
    // …repositories
}
```

This coexists with the top-level `includeBuild("frnk")` composite — Gradle dedupes the build even
though frnk also includes `build-logic` from its own `pluginManagement`.

Two plugins are host-facing (`frnk.kmp.library` for host-owned KMP modules, `frnk.android.sentry` for the application module):

- **`frnk.kmp.library`** *(optional)* — for host-owned KMP library modules: jvmToolchain 17 + Android
  SDK levels + bare iOS targets in one line, identical to what frnk's own modules get.
- **`frnk.android.sentry`** *(for every Android host — Sentry is mandatory)* — apply it in
  the **application** module:

  ```kotlin
  plugins {
      alias(libs.plugins.androidApplication)
      id("frnk.android.sentry")
  }
  ```

  The plugin takes no configuration, and the host does **not** declare the Sentry Gradle plugin
  version — frnk's catalog owns it, which is the point: two catalogs pinning the same plugin is a
  drift bug waiting to happen. It applies Sentry's Android Gradle plugin so the R8
  mapping's UUID lands in the manifest and the mapping uploads, making minified release traces
  symbolicate in Sentry. It needs no secret to build: the upload runs only when `SENTRY_AUTH_TOKEN`
  is in the environment (the release machine's — CI builds without it), with org/project from
  `SENTRY_ORG`/`SENTRY_PROJECT` or a gitignored `sentry.properties`. `autoInstallation` stays off
  (it would raise `sentry-android` above what the KMP SDK bundles) and bytecode instrumentation
  too (nothing in a Compose Multiplatform app benefits).

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
(`FrnkThemeConfig`, §2) — it flows into the `FrnkTheme` `FrnkApp` owns; omit it for the bundled palette.
A single-appearance host additionally passes `initialAppearance = Appearance.Light` (or `.Dark`) to seed
the `AppearanceController` once — theme palette, Android system-bar icon contrast, and the iOS interface
style then stop following the OS dark-mode setting (omit it to keep following the system, or when the
host restores a persisted appearance itself):

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
  `FrnkPaywallDestination`'s optional `onPurchased: (ProProduct) -> Unit` (backed by
  `PaywallEffect.Purchased`, emitted right before the `Dismiss` of a purchase that activated the
  entitlement) is where a host records its own conversion event — the `ProProduct` carries the plan
  and, from a store-backed provider, `price` (`ProPrice`: `amountMicros` + `currencyCode`).
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
