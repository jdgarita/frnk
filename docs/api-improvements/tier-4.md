# Tier 4 — Capability maturity

Larger / future work. Rounds out the toolkit but isn't blocking host adoption; each is a sizeable effort.

---

## 4.1 — Real `:camera` / `:permissions` implementations

- **Problem:** both are api-only scaffolds today — `NoopCameraController.capturePhoto()` always fails,
  `NoopPermissionController` returns `NotDetermined`/`Denied`. Hosts can compile against the contract but get
  no real behavior. (`frnk/capabilities/camera/`, `frnk/capabilities/permissions/`.)
- **Proposed change:** add `*-impl` modules with platform `expect/actual` implementations (CameraX/AVFoundation;
  the platform permission APIs), bound via `cameraModule`/`permissionsModule` overrides.
- **Host benefit:** camera + permission gating actually work without each host reimplementing them.
- **Effort:** L (native cinterop / platform APIs) · **Risk:** medium (new native surface; XCFramework impact) ·
  **Doc-only vs API:** API (new impl modules) · **Status:** Proposed

## 4.2 — Remote-config fetch guidance + state

- **Problem:** `RemoteConfigService` getters are synchronous while `fetchAndActivate()` is `suspend`; the docs
  don't say where to call it in the init flow, and there's no fetch-state signal. A host that forgets reads
  only bundled defaults, silently. (`:remote-config-api`.)
- **Proposed change:** document the canonical call site, and consider exposing fetch state (idle/loading/
  failed/last-fetched) so hosts can show progress / retry.
- **Host benefit:** predictable config loading; no silent "defaults-only" trap.
- **Effort:** S (docs) / M (state) · **Risk:** low · **Doc-only vs API:** doc-only or API (additive) ·
  **Status:** Proposed

## 4.3 — De-duplicate `EntitlementProvider` ↔ `EntitlementManager`

- **Problem:** `EntitlementManager` re-exposes the full `EntitlementProvider` method set (offerings/purchase/
  restore/managementUrl/refresh) plus god-mode — a DRY violation; a signature change means editing both.
  There are also multiple access patterns (`EntitlementManager`, `FeatureGate`, `ObserveProStatusUseCase`,
  `PaywallPurchaseUseCase`) a host must learn. (`:monetization-api`.)
- **Proposed change:** collapse the duplication (manager delegates without re-declaring) and document the one
  canonical access path per use case (gate vs paywall vs status).
- **Host benefit:** smaller, clearer monetization surface; less "which abstraction do I use?".
- **Effort:** M · **Risk:** medium (core monetization contract) · **Doc-only vs API:** API · **Status:** Proposed
