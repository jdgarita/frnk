# AGENTS

Guidance for AI coding agents (Codex, etc.) in this repository.

**`CLAUDE.md` is the canonical, detailed project guide — read it first.** This file is a thin pointer
so non-Claude agents land on the same rules; everything below is a summary of what `CLAUDE.md` covers
in full.

## Where knowledge lives

- **`CLAUDE.md`** — stable rules: architecture, module graph, toolchain pinning, conventions for adding code.
- **`docs/ARCHITECTURE.md`** — canonical module graph + api/impl split. Read before cross-module changes.
- **`docs/HOST_INTEGRATION.md`** — how a host app consumes the toolkit (coordinates, bootstrap, tokens, iOS umbrella, component style guide).
- **`REQUIREMENTS.md`** — the product spec + architecture invariants.
- **`.mobiai/brain/`** — living memory: *why* decisions were made, integration quirks (Firebase/RevenueCat), testing patterns, and bugfixes/workarounds. Query it with `mobiai brain context` / `mobiai brain search "<topic>"` before proposing architecture, DI, persistence, or integration changes.
- **Per-module `CLAUDE.md`** — module-specific rules (24 of them under `frnk/**`).

## Non-negotiables (full detail in `CLAUDE.md` / `REQUIREMENTS.md` §4)

- Make the smallest safe change; preserve module boundaries, naming, and style; avoid unrelated edits.
- `*-api` modules stay SDK-free; impls bind via Koin; every `*-api` interface returns `AppResult` (never throws).
- **No Material3 outside `:ui-bottom-nav`** — the design-system modules (`:ui-theme`/`:ui-components`/`:ui-scaffolds`) stay `compose-unstyled`-based.
- Hoist screen/nav/business state into `MviViewModel`; prefer `FrnkMviScreen`/`EffectCollector`/Navigation3 helpers over hand-rolled plumbing.
- New screens/features are exercised in `:demo-shared`, `demo-android`, and `iosDemoApp` (or a written justification).

## Validation (CI is paused on the private repo — validate locally before pushing)

```bash
./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache
./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache
```

- KMP modules: host tests run under `testAndroidHostTest` (not `testDebugUnitTest`); `:demo-android` (a `com.android.application`) uses `testDebugUnitTest`.
- Style is enforced by the `.githooks/pre-commit` ktlint hook, not CI.
- `local.properties` is gitignored and may be required for local builds. Never print secrets from it.

## Git safety

- The worktree may contain user changes — don't revert or overwrite them unless asked.
- Don't commit, push, merge, or open PRs unless explicitly requested.
- Before finishing, summarize changed files and the verification performed.
