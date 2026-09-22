# Architecture — system map

The one-page layered view of `frnk`. For the module-by-module graph, dependency rules, bootstrap
surface, and iOS linking contract, read the canonical **`docs/ARCHITECTURE.md`**. Where the two
disagree, `docs/ARCHITECTURE.md` wins.

## Shape

`frnk` is a **multi-module** Kotlin Multiplatform toolkit. All product code lives in `commonMain`;
Android and iOS are thin native wrappers around shared Compose Multiplatform UI.

```
frnk/
  core/          util · mvi · nav · platform · di        ← pure Kotlin, no Compose, no SDKs
  data/          db-api/impl · prefs-api/impl             ← persistence contracts + bindings
  capabilities/  analytics · identity · monetization (api/impl pairs) · camera · permissions (scaffolds)
                 haptics · camera · permissions · monetization-ui
  ui/            theme ← components ← scaffolds ← bottom-nav ← app   ← Compose Multiplatform
demo/
  shared/        FrnkDemoApp (one composable both platforms mount) → DemoKit.xcframework
  android-app/   MainActivity: initializeFrnk(...) + setContent { FrnkDemoApp() }
  ios-app/       SwiftUI shell: bootstrapDemoKoinWithSdks(...) + UIViewControllerRepresentable(MainViewController())
```

Gradle project names are flat (`:core-mvi`, `:ui-components`, `:demo-shared`, …) and remapped to
those directories in `settings.gradle.kts`.

- **Native wrappers are thin.** The Android `Activity` and the iOS `UIViewController` host one shared
  composable and bootstrap Koin. Platform-only work (`Purchases.configure`, SPM-linked SDKs) stays
  in the wrapper; nothing app-shaped lives there.
- **UI is 100 % Compose Multiplatform in shared code.** No SwiftUI screens, no XML layouts. The only
  `expect`/`actual` composable is the platform-adaptive bottom bar in `:ui-bottom-nav`.
- **Hosts consume modules, not an aggregator.** A downstream app depends on the individual modules it
  uses (`dev.jdgarita.frnk:<module>`) via composite build and builds its own iOS umbrella framework.

## Layers (Clean Architecture)

Dependencies point inward: **presentation → domain → data contracts**. Concrete data
implementations are bound at the edge by Koin and never imported by domain or presentation code.

### Data layer — `data/*-impl`, `capabilities/*-impl`

- **Local persistence: SQLDelight + `multiplatform-settings`.** The toolkit owns no schema. Hosts
  apply the SQLDelight plugin, own their `.sq` files and database class, and open it through the
  `SqlDriverFactory` SPI from `:data-db-api` (bound by `databaseModule` in `:data-db-impl`).
  Key-value state goes through `KeyValueStore` + the typed `Preference<T>` layer in
  `:data-prefs-api` (bound by `prefsModule`). Room is **not** used.
- **Networking.** The toolkit currently ships no HTTP client; remote capabilities go through
  PostHog (`:analytics-posthog`), Sentry (`:crash-sentry`) and RevenueCat (`:monetization-impl`). When a host or a future capability needs HTTP, it uses **Ktor** behind a
  new `*-api`/`*-impl` pair; Ktor never appears in an `*-api` module.
- **DTO → domain mapping happens here.** SDK types, SQLDelight rows, and wire DTOs are mapped to
  pure Kotlin domain models inside the impl module. Nothing above this layer sees an SDK type.
- Every impl wraps SDK calls in `runCatching` and returns `AppResult`; an unconfigured SDK degrades
  to a logged no-op, it does not crash the host.

### Domain layer — `core/util`, `*-api` modules

- **Pure Kotlin.** Interfaces, immutable models, sealed errors, and use cases
  (`DefaultSyncAuthUseCase`, `DefaultEntitlementManager`, `FeatureGate`). No Compose, no Ktor, no
  SDK, no SQLDelight driver, no platform imports.
- Every `*-api` interface returns **`AppResult<D, E : AppError>`** (sealed `Success` / `Failure` in
  `:shared-utils`) instead of throwing, so callers handle errors exhaustively.
- `IdentitySource` (`:identity-api`) is the single `identify(id)` contract shared by analytics,
  crash reporting, and billing — the one sanctioned api→api edge.

### Presentation layer — `core/mvi`, `core/nav`, `ui/*`

- **ViewModels expose `StateFlow`.** `MviViewModel<A, M, S, I, E>` (`:core-mvi`) owns a data-only
  model, derives an immutable `UiState` via a pure `mapToUiState`, consumes intents from a channel,
  and emits one-shot effects (navigation, toasts) through a single-consumer channel. Nothing
  Compose-specific lives in the ViewModel layer.
- **Compose bindings sit above.** `FrnkScreen(viewModel, arguments, onEffect) { state -> … }`
  attaches the VM and collects state lifecycle-aware; `FrnkNavDisplay` + Navigation3 routes handle
  type-safe navigation; the design system (`FrnkTheme` tokens, atoms, molecules, organisms,
  scaffolds) is `compose-unstyled`-based. Material3 exists only in `:ui-bottom-nav`.
- Composables are stateless: they read the collected `state` and dispatch intents.

## Dependency injection — Koin

- **Configured in `commonMain`.** Each module exports its bindings as a Koin `module`
  (`databaseModule`, `prefsModule`, `postHogAnalyticsModule(config)`, `sentryCrashReportingModule(config)`,
  `revenueCatModule`, `frnkUiModules()`, …). Only the `:camera` / `:permissions` scaffolds ship a
  no-op module (no impl exists yet); observability has none — PostHog + Sentry are mandatory on every
  host.
- **Initialised per platform.** Android calls `initializeFrnk(context, modules)`; iOS calls
  `initializeFrnk(modules)` (both in `:core-di`). The host passes **exactly** the module list it
  wants — capability selection is a module list, not an enum. Un-passed modules never enter the graph.
- **Interfaces over `expect`/`actual`.** Platform behaviour is a Kotlin interface in `commonMain`
  with a platform implementation bound in the platform source set's Koin module. `expect`/`actual`
  is reserved for tiny leaf primitives (`PlatformInfo`, the adaptive bottom bar, the
  subscription-management URL).

## Cross-cutting invariants

- `core/util` is the root; nothing in the graph depends upward.
- `*-api` never depends on an SDK; toolkit code never imports an `*-impl` package (demo modules are
  the sanctioned exception).
- Umbrella iOS frameworks that bundle `:monetization-impl` link with `-undefined dynamic_lookup`;
  the consuming Xcode project supplies the native RevenueCat/Sentry/PostHog symbols.
- Structured concurrency everywhere: ViewModels use `viewModelScope`, services take a scope, no
  global scopes.
