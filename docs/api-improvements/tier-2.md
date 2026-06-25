# Tier 2 — Host ergonomics

The high-value simplifications: reduce the boilerplate and footguns a host hits on day one. Prefer
**additive, non-breaking** changes (keep the explicit, no-magic paths intact).

---

## 2.1 — Cut the navigation-wiring boilerplate for the common app shape

- **Problem:** the standard app (onboarding → tab shell → paywall) makes a host hand-write a root
  `module { navigation<FrnkRootRoute.*> { … } }` **plus** a nested module **plus** thread an `onRootNavigate`
  lambda — 3 levels of nesting. See `demo/shared/.../navigation/modules/RootNavigationModule.kt` +
  `NestedNavigationModule.kt`. Dense Koin-nav3 DSL is the biggest day-1 friction.
- **Proposed change:** a higher-level helper or `FrnkApp` overload taking the onboarding/paywall destinations +
  the `customTab` + per-tab destination lambdas directly, so the common case is a few declarative lines. Keep
  the low-level `FrnkApp(onSavedStateConfiguration, onNavigationModule)` for full control.
- **Host benefit:** the most common integration becomes trivial; less Koin-DSL knowledge required.
- **Effort:** M–L · **Risk:** medium (new public entry point; must not regress the low-level path) ·
  **Doc-only vs API:** API (additive) · **Status:** Done
- **Shipped as:** `frnkTabbedRootModule(customTab) { home/custom/settings + optional onboarding/paywall }`
  + `FrnkTabNavigator` (`open`/`back`/`openPaywall`/`showOnboarding`) + `rememberFrnkRootStartRoute()` (first-launch
  gating via `OnboardingGate`) + a new `FrnkApp(startRoute = …)` param (additive, default `Onboarding`). Demo's
  `RootNavigationModule` + `NestedNavigationModule` collapsed into one `FrnkDemoApp` call (both deleted). The
  low-level `FrnkApp(onSavedStateConfiguration, onNavigationModule)` is unchanged.
- **Incidental pre-existing fixes** found while verifying iOS (unrelated to 2.1): `:ui-app`'s
  `ApplySystemBarAppearance` actual was in `iosArm64Main` (device-only) → moved to `iosMain` so the simulator
  target builds; `MainViewController` referenced the deleted `DemoEffect` → param removed. The demo's Swift
  `iosDemoApp` (`ComposeViewController.swift`/`ContentView.swift`) was then cleaned of the dead
  `DemoEffect`/toast routing (now hosts `MainViewController()` directly); `DemoKit.xcframework` assembles
  for both iOS arches. (The full Xcode app build still needs the local SPM / `GoogleService-Info.plist`
  setup the iOS README documents.)

## 2.2 — Optional bootstrap presets + fail-fast validation

- **Problem:** capability selection is an explicit module list (good — no magic), but the XOR rules
  (exactly one observability, one remote-config) and interdependencies (Settings needs monetization) are
  comment-only. A host can install both observability modules (silent shadowing) or omit a required one.
- **Proposed change:** add a thin typed preset builder (e.g. `frnkModules { observability = Firebase;
  monetization = revenueCat(key); database(...) }`) **and/or** a startup validation that fails fast on the
  documented rules. Additive — keep `initializeFrnk(modules = …)`.
- **Host benefit:** removes silent-shadowing / incomplete-stack footguns; less guesswork.
- **Effort:** M · **Risk:** low–medium · **Doc-only vs API:** API (additive) · **Status:** Proposed

## 2.3 — Decouple Settings from monetization — **Won't do**

- **Problem:** `SettingsViewModel` requires `ObserveProStatusUseCase` (`:monetization-api`) via Koin `get()`,
  so the Settings scaffold can't run unless monetization is installed.
- **Decision (2026-06-25):** **Won't do.** It's accepted that a monetization module is **always installed**,
  so the Settings scaffold's hard dependency on it is the intended design, not a wart. Do not add
  nullable/no-op/`getOrNull` fallbacks for monetization in Settings. (See the MobiAI brain / project memory
  "settings-may-depend-on-monetization".)

## 2.4 — `:data-db-api` Koin helper for host schemas

- **Problem:** every host hand-writes `single<MyDb> { MyDb(get<SqlDriverFactory>().create(MyDb.Schema,
  "x.db")) }` (see `HOST_INTEGRATION.md` §1 + the demo's `DemoDB` wiring).
- **Proposed change:** a small helper / Koin DSL (e.g. `databaseSingle(schema, name) { driver -> MyDb(driver) }`)
  in `:data-db-api`.
- **Host benefit:** removes repeated DB-binding boilerplate; one obvious path.
- **Effort:** S · **Risk:** low · **Doc-only vs API:** API (additive) · **Status:** Proposed

## 2.5 — Consolidate the iOS umbrella + Crashlytics-dSYM story

- **Problem:** standing up an iOS host means hand-writing the umbrella `XCFramework` + `export(...)` +
  `isStatic` + `linkerOpts("-undefined","dynamic_lookup")`, plus a fragile Crashlytics dSYM run-script build
  phase. `HOST_INTEGRATION.md` §6 admits agents must re-walk this checklist each time.
- **Proposed change:** one consolidated iOS-setup reference page and (ideally) a scaffold step in the
  `scaffold-kmp-app` skill that emits the umbrella `build.gradle.kts` + the Crashlytics build phase.
- **Host benefit:** the highest real-world onboarding cost becomes copy-paste-safe.
- **Effort:** M · **Risk:** low (tooling/docs) · **Doc-only vs API:** docs + tooling · **Status:** Proposed

## 2.6 — Implement `EffectCollector` (+ `SyncMviConfig`)

- **Problem:** the docs long promised these (now removed in Tier 1.1 as phantom). They are small, genuinely
  useful patterns: a standalone lifecycle-gated one-shot effect collector, and a persistent-VM config-sync
  helper. Today only `FrnkScreen` bundles effect collection (all-or-nothing).
- **Proposed change:** implement `EffectCollector(effects, minActiveState, onEffect)` and (optionally)
  `SyncMviConfig(viewModel, config, asIntent)` in `:ui-scaffolds` `ui/mvi/`, then document them.
- **Host benefit:** consume effects / sync config without adopting the full `FrnkScreen` template.
- **Effort:** S–M · **Risk:** low (additive) · **Doc-only vs API:** API (additive) · **Status:** Proposed
  (moved from Tier 1)
