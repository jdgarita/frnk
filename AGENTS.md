# AGENTS

Codex guidance for this repository. Follow this file first, and use `CLAUDE.md` as the canonical detailed project guide.

## Project

- `frnk` is a Kotlin Multiplatform + Compose Multiplatform toolkit, not a standalone product app.
- Downstream apps consume it as a Git submodule/composite build via `includeBuild("../frnk")`.
- `androidDemoApp` and `iosDemoApp` are smoke harnesses only.
- Read `docs/ARCHITECTURE.md` before cross-module architecture changes.

## Repo Shape

- Most shared domain modules keep flat Gradle paths (`:shared-ui-atoms`, `:shared-database-api`, etc.).
- Backend modules use nested Gradle paths under `:shared:backend` (`:shared:backend:api`, `:shared:backend:firebase`).
- Most flat shared module files live physically under `shared/<module-name>/`; backend module files live under `shared/backend/<name>/`.
- There is no aggregator (restructure Stage 1): consumers depend on individual modules. `:core-di` owns `initializeFrnk(modules)`; `:ui-app` owns `FrnkAppScaffold` + `frnkUiModules()`.
- Backend/database/monetization modules use an api/impl split. Do not add SDK dependencies to `*-api` modules.

## Commands

Use targeted checks when possible:

```bash
./gradlew compileAndroidMain
./gradlew testAndroidHostTest
./gradlew :androidDemoApp:compileDebugKotlin
./gradlew :androidDemoApp:testDebugUnitTest
./gradlew :shared-database-impl:testAndroidHostTest
./gradlew ktlintFormat
./gradlew assemble
./gradlew :shared-demo:assembleDemoKitDebugXCFramework
```

`local.properties` is gitignored and may be required for local builds. Do not print secrets from it.

## Toolchain

- JDK 17.
- Kotlin 2.4.0, AGP 9.2.1, Gradle 9.5.1.
- AGP 9 KMP modules use `com.android.kotlin.multiplatform.library` and `kotlin { android { ... } }`, not `com.android.library` or a top-level `android {}` block.
- `androidDemoApp` uses AGP 9 built-in Kotlin. Do not add `kotlin.android`.
- SDK versions live in `gradle/libs.versions.toml`; non-SDK constants live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt`.

## Code Rules

- Make the smallest safe change and avoid unrelated edits.
- Preserve module boundaries and existing naming/style.
- Return `AppResult` from domain APIs; do not throw for expected failures.
- Keep third-party SDKs out of `*-api` modules.
- Do not add Material3 outside `shared-ui-nav`; `shared-ui-atoms` stays `compose-unstyled` based.
- Prefer `MviViewModel`, `FrnkMviScreen`, `EffectCollector`, and Navigation3 helpers over hand-rolled state/effect/navigation plumbing.
- UI state should be hoisted into ViewModels. Use local Compose state only for local UI holders such as scroll, focus, animation, and remembered builders.
- New screens/features should be represented in `shared-demo`, `androidDemoApp`, and `iosDemoApp` unless clearly inapplicable.

## Testing

- Validate with the narrowest useful Gradle task first.
- KMP host tests run with `testAndroidHostTest`, not `testDebugUnitTest`.
- `androidDemoApp` is a normal Android app, so its tests use `testDebugUnitTest`.
- Put platform-agnostic logic tests in `commonTest`; platform runtime tests go in `androidHostTest`.
- `shared-ui-atoms` Compose UI tests live in `androidHostTest` with Robolectric.

## Git Safety

- The worktree may contain user changes. Do not revert or overwrite them unless explicitly asked.
- Do not commit, push, merge, or open PRs unless explicitly requested.
- Before finishing, summarize changed files and verification performed.
