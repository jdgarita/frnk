# Tier 3 — Consistency & type-safety

Polish: tighten naming, state-shape conventions, and type safety. Lower urgency than Tiers 1–2; some items are
churny, so weigh against blast radius.

---

## 3.1 — Type-safe feature gating

- **Problem:** `Feature` is a `data class` with companion constants (`Feature.Premium`, …). A host can call the
  gate with `Feature("typo")` and silently get `false` — no compile-time safety. (`:monetization-api`.)
- **Proposed change:** offer a typed registry / sealed or enum-backed feature pattern (or a host-extensible
  `sealed` hierarchy) so gates are checked at compile time.
- **Host benefit:** premium gating can't silently misfire on a typo.
- **Effort:** M · **Risk:** medium (touches the monetization contract) · **Doc-only vs API:** API ·
  **Status:** Done — `Feature` is now an open marker `interface Feature { val id: String }`; the toolkit's
  catalogue is `enum class FrnkFeature(override val id) : Feature`, and hosts implement `Feature`
  (typically via their own enum), so `Feature("typo")` no longer compiles. `FeatureGate` matches
  `freeFeatures` by `id` for correctness across any `Feature` impl.

## 3.2 — Unify / clearly document the component `*State` convention

- **Problem:** the mandated shape is `sealed interface FrnkXState { data class Content; data object Skeleton }`,
  but there are divergences: `FrnkTextState` is a `sealed class` with per-variant `skeleton` fields,
  `FrnkEmptyState` is a plain `data class` with no skeleton, and several atoms carry secondary constructors
  (`ImageVector` → `FrnkIconSource.Vector`, `String` → `FrnkStringSource.Raw`) that double the surface.
  (`:ui-components` `ui/atoms/`, `ui/molecules/`, `ui/organisms/`.)
- **Proposed change:** either align the outliers or document the exceptions crisply in the component style
  guide (`HOST_INTEGRATION.md` §9 + `frnk/ui/components/CLAUDE.md`) with the rationale for each.
- **Host benefit:** faster to learn; fewer "why is this one different?" moments when authoring components.
- **Effort:** M (align) or S (document) · **Risk:** medium if aligning (touches many atoms) ·
  **Doc-only vs API:** doc-only or API · **Status:** Done (document, not align) — every divergence is
  justified, so aligning would be net-negative (a sealed *interface* can't carry `FrnkDivider`/`FrnkText`'s
  shared `open val` fields; a `Skeleton` object on a terminal empty state is nonsense; the secondary
  constructors are deliberate ergonomics). Instead documented a **three-category `*State` taxonomy** (A
  Stateful sealed-interface · B Variant shared-field sealed class · C Single-state data class) + the
  ergonomic-secondary-constructor pattern in `HOST_INTEGRATION.md` §9 and `frnk/ui/components/CLAUDE.md`,
  **corrected** the `FrnkDivider` "non-sealed" mislabel (it's a Category-B sealed class), and added a
  one-line `State shape — Category X` marker to each outlier `*State`. No API change.

## 3.3 — Tighten `FrnkRoute` / `FrnkRootRoute` naming + config asymmetry

- **Problem:** two parallel route catalogues — `FrnkRoute` (tab-level) and `FrnkRootRoute` (app-root) — both
  with a `Custom(id)`, confuse first-time hosts. The config builders are asymmetric too: `frnkRootNavConfig`
  is a `val`, `frnkNestedNavConfig(hostRoutes)` is a function. (`:core-nav`.)
- **Proposed change:** clarify naming/docs (which catalogue keys which stack) and consider symmetric config
  builders (or document the asymmetry's reason: root is fixed, nested is host-extensible).
- **Host benefit:** less mental overhead at the most error-prone layer.
- **Effort:** S–M · **Risk:** medium if renaming public types · **Doc-only vs API:** doc-only (or API if
  renaming) · **Status:** Done (rename + prune + symmetric config) — the tab-level catalogue was renamed
  `FrnkRoute` → **`FrnkTabRoute`** so the level is explicit in the type name (`FrnkRootRoute` keeps its
  name), and its vestigial `Onboarding`/`Paywall` members were **pruned** (full-screen flows live on
  `FrnkRootRoute`); `:shared-monetization-ui`'s paywall helpers (`frnkPaywallNavigation` +
  `rememberFrnkSettingsHandler`) were repointed to **`FrnkRootRoute.Paywall`** (the paywall is a
  `FrnkFullScreenRoute`, so it belongs above the bottom bar). The config asymmetry was a real capability
  gap, not just cosmetics: `frnkRootNavConfig` became a **function symmetric** with
  `frnkNestedNavConfig(hostRoutes)` — it now registers `FrnkRootRoute.Custom` (the old `val` form silently
  dropped it) and merges host root routes, so the root stack is host-extensible too. Nested/tab stacks are
  in-memory only, so the `FrnkTabRoute` `serialName` change needs no persistence migration; `FrnkRootRoute`
  was not renamed, so the process-death-persisted root stack is unaffected. Covered by `FrnkTabRouteTest`
  + the new `RootNavConfigTest`.

## 3.4 — `Preference<T>` coverage

- **Problem:** the typed `Preference<T>` layer (`:data-prefs-api`) supports only `String`/`Boolean`/`Int`/
  `Enum`; `Long`, `Double`, and nullable-string are deferred, so hosts drop to raw `KeyValueStore`.
- **Proposed change:** add `longPreference` / `doublePreference` / nullable-string support.
- **Host benefit:** hosts stay on the typed delegate layer.
- **Effort:** S · **Risk:** low (additive) · **Doc-only vs API:** API (additive) · **Status:** Proposed
