# Spike: Platform-Adaptive Bottom Navigation Bar — Haze vs Calf

- **Branch:** `spike/adaptive-bottom-nav`
- **Date:** 2026-06-02
- **Status:** Spike complete — both approaches built, compiled, and linked on Android + iOS. Recommendation below.
- **Goal:** A bottom nav that reads as native on each platform (a frosted translucent bar on iOS, the
  floating pill on Android), shipped as a reusable, host-themeable toolkit component. Android must stay on
  `compose-unstyled` (no Material3). iOS should lean on Compose Multiplatform as much as possible.

## TL;DR

The two libraries solve **different** problems and are not mutually exclusive:

- **Haze** is a blur/frosted-glass *modifier*. It has **no Material3 dependency** (core `haze` + `haze-blur`),
  renders in pure Compose/Skia on iOS, and slots cleanly into a `compose-unstyled` atom. **Recommended.**
- **Calf** is a *nav-bar component* that renders a genuine native UIKit `UITabBar` on iOS — but it
  **hard-depends on Material3**, so it can never live in `shared-ui-atoms`, and its UITabBar is UIKit-drawn
  (ignores `FrnkTheme`, `LocalFrnkHaptics`, the collapsible-bars coordinator, and Haze). Useful as a
  reference for the "truly native" feel, but **not recommended** for the shipped component.

**Recommendation: ship `FrnkAdaptiveBottomNavBar` (Haze-based).** Keep the Calf wrapper in `:shared-demo`
only, as the A/B reference. See [Recommendation](#recommendation).

## What was built

A live three-way A/B harness, switchable from a segmented control on the demo's Home tab ("Pill / Haze / Calf"):

| Variant | File | iOS | Android |
|---|---|---|---|
| **Pill** (control) | existing `FrnkBottomNavBar` | floating pill | floating pill |
| **Haze** (candidate) | `FrnkAdaptiveBottomNavBar` (`shared-ui-atoms`) | full-width frosted bar (icon+label, blurs content behind it, fills the home-indicator safe area) | delegates to the pill |
| **Calf** (reference) | `CalfAdaptiveBottomNavBar` (`shared-demo` only) | native UIKit `UITabBar` | Material3 `NavigationBar` |

Key implementation choices:

- **`FrnkAdaptiveBottomNavBar`** takes an explicit `FrnkAdaptiveNavStyle` (`IosFrostedBar` /
  `AndroidFloatingPill`) rather than reading `PlatformInfo`. Atoms are `commonMain`-only with no `shared-utils`
  dependency, and an explicit param stays previewable/testable per shape (the repo's `FrnkButtonVariant`
  precedent). The **host** derives the per-platform default — the demo uses `PlatformInfo.osName`.
- The Android branch **delegates to the existing `FrnkBottomNavBar`**, so there is a single source of the pill
  geometry/behaviour and zero duplication.
- `HazeState` is **hoisted at the demo host**: the `FrnkNavHost` content is the `hazeSource`, the bar is the
  `hazeEffect`. Threading `hazeSource` *into* `FrnkScreenScaffold` would be cleaner long-term but is a toolkit
  API change, deliberately out of scope for the spike.
- `haze` core is exposed `api` from `shared-ui-atoms` because `HazeState` is part of the atom's public
  signature; `haze-blur` stays `implementation` (the blur is an internal detail). `haze-materials` is **not**
  taken — it is the only Material3-adjacent Haze module.
- No skeleton on the bar, by design (persistent navigation chrome, not a loading surface — the `FrnkEmptyState`
  rationale).

## Findings vs the original plan

- **Haze version.** The plan named `1.7.2`, but that line targets CMP 1.10 and has no separate `haze-blur`
  module. The line that matches our stack (Kotlin 2.3.20 / CMP 1.11) and ships the `haze` + `haze-blur` split is
  **`2.0.0-alpha02`**. It compiles and links on Android **and** iOS Native under our exact Kotlin 2.3.21 /
  CMP 1.11.0 — the plan's #1 risk is retired. Caveat: it is an **alpha**.
- **Calf ImageVector→UIImage friction was overstated.** Calf 0.12.0 ships `UIKitImage.Vector(imageVector)`,
  which rasterises the `ImageVector` to a `UIImage` on iOS — so mapping `FrnkBottomNavItem.icon` is a one-liner,
  no manual asset work.
- **M3 quarantine holds and DemoKit still links.** Material3 + Calf appear only in `:shared-demo`
  (`build.gradle.kts`, the Calf wrapper) and the version catalog. A grep across `shared-ui-atoms`, `:shared`,
  `shared-ui-api`, `shared-utils`, `iosApp`, `androidApp` finds them only in `.md` prose. The
  `DemoKit.xcframework` assembles successfully with Calf/M3 on the classpath under the existing
  `dynamic_lookup` (no new SPM package needed).

## Haze — pros / cons

**Pros**
- No Material3; stays inside `shared-ui-atoms` and the `compose-unstyled` rule.
- Pure Compose/Skia on iOS — honours "use CMP as much as possible". One atom, host-themeable via `FrnkTheme`
  tokens (the frost colour is `colorSurface`); participates in the collapsible-bars coordinator, the ambient
  ripple, and `LocalFrnkHaptics` like every other atom.
- The frosted look is the dominant signal that makes a Compose-drawn bar read as iOS-native.
- Android < 12 degrades to a flat tint automatically (the accepted fallback; `MIN_SDK = 26`).

**Cons / risks**
- **Alpha dependency** (`2.0.0-alpha02`). Acceptable for a spike; for shipping, pin carefully and watch for the
  stable 2.0 (or re-evaluate against a CMP-1.11-compatible 1.x if one appears).
- `HazeState` must be shared between the scrolling content and the bar — it crosses the atom↔scaffold↔host
  boundary. The spike wires it through the demo host; productionising means deciding whether
  `FrnkScreenScaffold`/`BottomNavScaffold` should own/forward the `hazeSource`.
- Blur is GPU work; perf on low-end Android and the iOS Skia shader needs on-device measurement (see below).
- It is *not* a native control — it is a faithful imitation. Fine for this goal; not the same as a real UITabBar.

## Calf — pros / cons

**Pros**
- A genuine native iOS `UITabBar` (real system look/behaviour) with little custom code; Material3
  `NavigationBar` elsewhere. `UIKitImage.Vector` handles icon rasterisation.

**Cons**
- **Hard `compose.material3` dependency** — disqualifies it from `shared-ui-atoms` / any shippable toolkit
  module. It can only live in `:shared-demo`.
- The iOS `UITabBar` is UIKit-drawn: it does **not** pick up `FrnkTheme` tokens (brand colours diverge), does
  not fire `LocalFrnkHaptics`, and does not participate in the collapsible-bars coordinator or Haze sampling.
- Experimental API (`@OptIn(ExperimentalCalfUiApi::class)`); the iOS vs Material configuration surfaces are
  entirely separate parameter sets.
- Cuts against "use CMP as much as possible on iOS" — its whole value is UIKit interop.

## Recommendation

1. **Promote `FrnkAdaptiveBottomNavBar` (Haze) to a real toolkit atom.** It satisfies every constraint: no
   Material3, `compose-unstyled`, host-themeable, CMP-on-iOS, and it reads as native on each platform.
   Before shipping: (a) decide the stable Haze version / pinning story (currently alpha), and (b) decide where
   `hazeSource` lives — ideally forwarded by `FrnkScreenScaffold`/`BottomNavScaffold` so hosts don't hand-wire
   it. Update `shared-ui-atoms/CLAUDE.md` and reserve content-inset behaviour per style.
2. **Do not adopt Calf** for the shipped component. Keep the demo wrapper as the A/B reference only. If a *truly*
   native iOS tab bar ever becomes a hard requirement, prefer a thin first-party UIKit-interop `expect/actual`
   in `iosMain` over pulling Material3 into `commonMain` via Calf.

## Code-review follow-ups

A high-effort review of the spike surfaced 10 findings. **Fixed in this branch:**

- **iPad fell back to the Android pill** — `PlatformInfo.osName` is `"iPadOS"` on iPad (not `"iOS"`); the
  host default now matches both Apple OS names. (Was a real bug, propagated by the recommended host snippet.)
- **iOS frosted bar safe-area accounting** — `barHeight(style)` excluded the `navigationBars` inset the bar
  consumes, so the bar only partially hid on collapse and under-reserved content space on notched iPhones.
  Added `FrnkAdaptiveBottomNavBarDefaults.barHeightWithSafeArea(style)` (content + inset); the demo uses it
  for both the collapse offset and the per-variant content reserve.
- **Misuse-prone `hazeState`** — now nullable (`null` → flat translucent fallback), so pill-only hosts never
  construct one and a forgotten `hazeSource` degrades gracefully instead of silently.
- **Disabled-state alpha** — moved off the frost layer onto the chrome, so disabling no longer dims the
  blurred backdrop.
- **Always-on `hazeSource`** — now attached only when the Haze variant is active (it's not free).
- **Fragile Int-index nav state** — the demo holds the `DemoNavVariant` enum (via `rememberSaveable`) and
  converts to/from an index only at the `FrnkSegmentedControl` boundary.

**Consciously deferred to promotion (out of scope for a spike):**

- **`api(libs.haze)` exposes the alpha in FrnkKit's surface.** `HazeState` is part of the atom's public
  signature, so it must be `api`. Dropping it to `implementation` requires the scaffold to own the
  `HazeState` (e.g. via `FrnkScreenScaffold`/`BottomNavScaffold` + a composition local) so it never appears
  in the atom signature — the same refactor that fixes the hand-wired `hazeSource` altitude issue. Do this at
  promotion, together with pinning a stable Haze (currently `2.0.0-alpha02`).
- **Shared `FrnkNavItem` primitive.** `IosFrostedItem` duplicates the pill's per-item interaction language
  (selection tint, haptic-on-change, `ProvideContentColor`). Extracting a shared item primitive touches the
  existing shipped `FrnkBottomNavBar`, so it belongs to the promotion PR, not the spike.

## Verification performed

All green on `spike/adaptive-bottom-nav` (Kotlin 2.3.21 / CMP 1.11.0 / AGP 9.2.1):

```
:shared-ui-atoms:compileAndroidMain                  ✅
:shared-ui-atoms:compileKotlinIosSimulatorArm64      ✅   (Haze on iOS Native)
:shared-demo:compileAndroidMain                      ✅   (Calf + Material3)
:shared-demo:compileKotlinIosSimulatorArm64          ✅   (Calf native UITabBar actual + Haze)
:shared-demo:assembleDemoKitDebugXCFramework         ✅   (Calf/M3 link under dynamic_lookup)
:shared-demo:testAndroidHostTest                     ✅
:shared-ui-atoms:ktlintCheck / :shared-demo:ktlintCheck  ✅
:androidDemoApp:compileDebugKotlin                   ✅
M3/Calf quarantine grep                              ✅   (only :shared-demo + catalog)
```

**Not yet done (requires running the apps on device/simulator):** the actual *visual* and *performance*
comparison — live blur quality, frost over scrolling content, collapsible-bars + `hazeSource` interaction at
the safe-area edge, Android < 12 tint fallback, and the iOS frame cost of the Skia blur shader. The harness is
in place (toggle on the Home tab); these need a manual pass on both platforms before a ship decision.
