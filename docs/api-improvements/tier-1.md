# Tier 1 — Hygiene / honesty

Fast, low-risk items: docs + dead-code, zero behavior change. These are the "make the public surface honest"
wins; mostly additive-or-deletion.

---

## 1.1 — Resolve the phantom MVI-binding docs (docs-only)

- **Problem:** `FrnkMviScreen`, `EffectCollector`, `SyncMviConfig`, and `RememberMviLifecycle` are documented
  as public API in `CLAUDE.md`/KDoc but **do not exist** in code. The only real binding is `FrnkScreen`
  (`frnk/ui/scaffolds/src/commonMain/kotlin/dev/jdgarita/frnk/ui/mvi/FrnkScreen.kt` — the lone file in
  `ui/mvi/`). Hosts reading the docs go looking for API that isn't there.
- **Proposed change (decided: docs-only):** delete the four phantom symbols from the docs and point readers at
  `FrnkScreen`. Touch `frnk/ui/scaffolds/CLAUDE.md` (the `ui/mvi/` bullets), root `CLAUDE.md` ("New screen"
  convention), `docs/HOST_INTEGRATION.md` (§3), and KDoc that mentions them in
  `frnk/ui/bottom-nav/.../FrnkNestedNavScaffold.kt`, `frnk/ui/scaffolds/.../FrnkBottomBarInset.kt`,
  `frnk/core/nav/.../NavBackStackExt.kt`.
- **Host benefit:** the docs (a host's contract) match reality; no chasing non-existent helpers.
- **Effort:** S · **Risk:** none (docs only) · **Doc-only vs API:** doc-only · **Status:** Done
- **Note:** actually *implementing* `EffectCollector` (+ `SyncMviConfig`) was considered and moved to
  [tier-2.md](tier-2.md#26--implement-effectcollector--syncmviconfig) — they're small and genuinely useful, but
  adding API is beyond Tier 1's hygiene scope.

## 1.2 — Delete the dead legacy config cluster

- **Problem:** a self-contained cluster of legacy `*Config` types remains public though nothing consumes it
  (they reference only each other + two KDoc links). Files:
  - `:ui-app`: `FrnkAppConfig.kt` (declares `FrnkAppConfig` + `FrnkMonetizationConfig`), `ext/FrnkAppConfigExt.kt`
  - `:ui-bottom-nav`: `FrnkTabbedNavConfig.kt` (declares `FrnkTabbedNavConfig`, `FrnkAppInfo`, `FrnkNavConfig`,
    `FrnkHomeConfig`, `FrnkSettingsConfig`, `FrnkOnboardingConfig`), `FrnkTabbedNavViewState.kt`,
    `FrnkBottomNavState.kt`, `FrnkBottomNavTab.kt`, `FrnkFeatureItem.kt`
  - dangling `[FrnkAppConfig]` KDoc link in `frnk/ui/app/.../FrnkApp.kt`.
  These are leftovers from the removed `FrnkAppScaffold`/`FrnkTabbedNavScaffold` batteries-included path.
- **Proposed change:** `git rm` the 7 files; reword the `FrnkApp.kt` KDoc so no broken `[FrnkAppConfig]` link;
  remove the now-empty `ui/app/.../ext/` dir if empty.
- **Host benefit:** smaller, honest public surface; removes "which entry point is real?" confusion (`FrnkApp`
  is the one true root).
- **Effort:** S · **Risk:** low (unused by demo; pre-1.0, CI paused) · **Doc-only vs API:** API (public-type
  removal) · **Status:** Done

## 1.3 — Doc-drift sweep

- **Problem:** docs reference removed/renamed API: `FrnkAppScaffold`, the `FrnkAppConfig`/`FrnkTabbedNavConfig`
  "batteries-included superset", and the "Legacy remnants pending removal" note in
  `frnk/ui/bottom-nav/CLAUDE.md` (now actually removed by 1.2).
- **Proposed change:** sweep root `CLAUDE.md`, `frnk/ui/bottom-nav/CLAUDE.md`, and `docs/HOST_INTEGRATION.md`
  for these stale references; confirm no leftover `parametersOf` (bottom-nav) or other already-fixed mentions.
- **Host benefit:** trustworthy docs end-to-end.
- **Effort:** S · **Risk:** none (docs only) · **Doc-only vs API:** doc-only · **Status:** Done

## 1.4 — Sweep remaining `FrnkAppScaffold` / `FrnkTabbedNavScaffold` references

- **Problem:** both symbols were deleted in earlier work but are still referenced in KDoc/docs (not code):
  `REQUIREMENTS.md`, the demo's `MainViewController.kt` / `DemoRoutes.kt` / `MainActivity.kt` KDoc,
  `OnboardingGate.kt`, `FrnkUiModules.kt` (+ `FrnkUiModulesTest.kt`), and `FrnkRootRoute.kt`. (The
  user-facing `requireFrnkKoin` error message and the two "pending removal" doc notes were already fixed in
  1.2/1.3.)
- **Proposed change:** replace these dangling references with `FrnkApp` / `FrnkNestedNavScaffold` (or drop the
  sentence) so no doc names a deleted symbol.
- **Host benefit:** no references to nonexistent APIs anywhere.
- **Effort:** S · **Risk:** none (docs/KDoc) · **Doc-only vs API:** doc-only · **Status:** Done

---

### Verification (Tier 1)
- `./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache` — must stay green
  (the cluster is unused).
- Grep confirms zero references to every deleted symbol.
- No device run / no tests — zero behavior change.
