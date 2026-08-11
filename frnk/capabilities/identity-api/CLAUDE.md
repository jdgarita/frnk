# :identity-api

Pure-interface identity contract. **No Firebase, no SDK of any kind** — the Firebase Auth binding
lives in `:identity-impl` (`firebaseIdentityModule`).

## Contents

- `AnonymousIdentityProvider.kt` — the **producer** of an identity: `uid: StateFlow<String?>`,
  `ensureSignedIn()`, and `idToken(forceRefresh)` (the signed JWT a backend verifies to derive the
  uid). Bound to Firebase Auth by `:identity-impl`; hosts that don't want Firebase bind their own.
- `IdentitySource.kt` — the **consumers** of an identity:
  `suspend fun identify(id: String): AppResult<Unit, IdentityError>`, plus the `IdentityError` enum
  (single `Error` variant — the sinks cannot distinguish causes, so pretending otherwise would be
  fiction).

## Why `IdentitySource` lives here

It is implemented by four interfaces across two other modules — `AnalyticsTracker` and
`CrashReporter` (`:analytics-api`), `EntitlementProvider` and `EntitlementManager`
(`:monetization-api`) — so every consumer of a user identity takes it through one identically-shaped
call, and a host never has to remember four different log-in signatures.

This module is its natural home: it already owns identity and depends on nothing but
`:shared-utils`, so putting the contract here creates no cycle. It is also the reason for the single
cross-capability api→api edge **`analytics-api ← identity-api`** (see `docs/ARCHITECTURE.md`); adding
that edge was preferred over duplicating the contract or inventing a fifth module for one interface.

`DefaultSyncAuthUseCase` (`:monetization-api`) is the orchestrator that resolves a uid from
`AnonymousIdentityProvider` and fans it out to every `IdentitySource`.

## Rules

- **No SDK dependencies.** Anything touching `dev.gitlive.firebase.*` belongs in `:identity-impl`.
- Every method returns `AppResult`, never throws — the toolkit-wide `*-api` rule.
- `identify(id)` takes a **non-null** id: there is deliberately no logout/clear path yet, because no
  frnk host has a real account system. Adding one means widening this signature, and note the
  asymmetry it has to absorb — gitlive's analytics `setUserId` accepts `String?` while its
  crashlytics equivalent requires non-null.
- Keep `IdentityError` coarse. A sink that genuinely can distinguish failure causes should model
  them in its own error type rather than inflating this shared one.

## Dependencies

- `api(projects.sharedUtils)` only (`AppResult` / `AppError`), plus coroutines for the `StateFlow`.

## Testing

No tests of its own — it is interfaces and one enum. The contract is exercised where it is
implemented: `ObservabilityTest` (`:analytics-api`), `DefaultSyncAuthUseCaseTest` and
`DefaultEntitlementManagerTest` (`:monetization-api`), `FirebaseAuthManagerTest` (`:identity-impl`).
