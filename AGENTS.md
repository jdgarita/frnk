# AGENTS

Instructions for AI coding agents (Claude Code, Codex, Gemini CLI, Cursor, …) working in this repository.

**`CLAUDE.md` is the canonical, detailed project guide — read it first.** This file is the
cross-agent summary: scope, commands, working rules, and Git rules. `ARCHITECTURE.md` (system map)
and `CONVENTIONS.md` (coding rules) sit next to it at the repo root.

## Scope

`frnk` is a **reusable Kotlin Multiplatform + Compose Multiplatform starter toolkit**, not a
standalone app. It is the foundational template every downstream indie app (e.g. *Still*, *Faint*)
consumes as a Git submodule through a Gradle composite build (`includeBuild("../frnk")`).

- Everything under `frnk/{core,data,ui,capabilities}/` is **shipping toolkit API**. Treat public
  surface changes as breaking for every host app.
- `demo/{shared,android-app,ios-app}` are **internal smoke harnesses** only. They prove a feature
  works on both platforms; they are never the product.
- The toolkit owns **no app schema, no app routes, no app screens** — hosts own those. Keep template
  code generic; do not bake one app's needs into the toolkit.

## Where knowledge lives

- **`CLAUDE.md`** — stable rules: module graph, toolchain pinning, conventions for adding code.
- **`ARCHITECTURE.md`** (root) — the layered system map; **`docs/ARCHITECTURE.md`** — the canonical
  module-by-module graph and dependency rules. Read both before cross-module changes.
- **`CONVENTIONS.md`** — coding rules (DI, UI, immutability, errors, platform boundaries).
- **`docs/HOST_INTEGRATION.md`** — how a host app consumes the toolkit.
- **`REQUIREMENTS.md`** — product spec + architecture invariants.
- **`.mobiai/brain/`** — living memory: the *why* behind decisions, integration quirks
  (Firebase/RevenueCat/dSYM), testing patterns, bugfixes. Query with `mobiai brain context` /
  `mobiai brain search "<topic>"` before proposing architecture, DI, persistence, navigation, or
  integration changes; save new decisions there, not in the docs.
- **Per-module `CLAUDE.md`** files under `frnk/**` — module-specific rules.

## Commands

One-time bootstrap per checkout (`BuildKonfig` fails at configuration time without it):

```bash
cp local.properties.template local.properties   # then fill in FIREBASE_* + BUILD_VARIANT
```

Build:

```bash
./gradlew build                                  # full build + check on every target (slow; release gate)
./gradlew assemble                               # every target's artifacts, no tests
./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache   # fast compile gate
./gradlew :demo-shared:assembleDemoKitDebugXCFramework   # DemoKit.xcframework for demo/ios-app
```

Test:

```bash
./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache   # standard test gate
./gradlew iosSimulatorArm64Test                  # Kotlin/Native tests on the iOS simulator target
./gradlew allTests                               # every target, aggregated report
./gradlew :data-prefs-api:testAndroidHostTest    # one module
```

Clean and style:

```bash
./gradlew clean
./gradlew ktlintFormat                           # also runs from the pre-commit hook
```

Notes:

- KMP modules run host tests under **`testAndroidHostTest`**, not `testDebugUnitTest`. Only
  `:demo-android` (a `com.android.application`) uses `testDebugUnitTest`.
- `compileAndroidMain` covers `commonMain` + `androidMain` for every KMP module (the AGP 9 task name;
  `compileDebugKotlinAndroid` does not exist for KMP modules).
- CI is paused while the repo is private. **Validate locally before every push** with the compile
  gate and the test gate above.
- `demo/ios-app` needs a local `GoogleService-Info.plist` and Xcode-resolved Swift packages; it is not
  buildable from a fresh clone and CI never builds iOS.

## Rules for AI agents

- **Plan before large refactors.** Anything that touches more than one module, moves a public type,
  changes a Koin module's surface, or alters the module graph starts in planning mode: read
  `ARCHITECTURE.md` + `docs/ARCHITECTURE.md`, query the brain, write the plan, get it approved, then
  execute. Do not improvise multi-module changes.
- **Smallest safe change.** Preserve module boundaries, naming, and style. No drive-by refactors or
  unrelated edits.
- **Structured concurrency, bounded scopes.** Every coroutine lives in a scope with a lifecycle that
  owns it: `viewModelScope` in ViewModels, `rememberCoroutineScope` / `LaunchedEffect` in
  composables, a caller-supplied scope in services. Never create a `GlobalScope` or an unbounded
  `CoroutineScope()` field; never `runBlocking` outside tests; never catch `CancellationException`.
  Suspend functions are main-safe (switch dispatchers inside, not at the call site). See
  `CONVENTIONS.md`.
- **Respect the api/impl split.** `*-api` modules stay SDK-free; impls bind via Koin; every `*-api`
  interface returns `AppResult` and never throws.
- **No Material3 outside `:ui-bottom-nav`.** The design system is `compose-unstyled` only.
- **Hoist state.** Screen / navigation / business state lives in an `MviViewModel`, never in
  `remember { mutableStateOf(...) }`.
- **Demo every feature in all three demo layers** (`:demo-shared`, `demo-android`, `iosDemoApp`) or
  say explicitly why it cannot be demoed.
- **Verify before claiming done.** Run the compile gate and the test gate; report failures verbatim.
- **Record decisions in the brain**, not in the docs. Docs stay lean and canonical.
- `local.properties` and `GoogleService-Info.plist` hold secrets. Never print or commit them.

## Git rules

- **Commit frequently** at logical checkpoints with descriptive, intent-based messages
  (`feat(ui-components): add FrnkChip atom`, `fix(core-nav): keep tab stacks across config change`).
  Small commits make review and bisecting cheap.
- **Never push automatically.** Never push, merge, tag, or open a PR unless the user explicitly asks
  in that turn. Approval to push once does not carry over.
- The pre-commit hook runs `ktlintFormat` and re-stages fixed files. Do not bypass it
  (`--no-verify` / `SKIP_KTLINT=1`) unless the user asks.
- The worktree may contain the user's uncommitted changes. Never revert, reset, or overwrite them.
- Before finishing, summarize changed files and the verification performed.
