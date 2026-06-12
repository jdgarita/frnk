# AGENTS

Codex guidance for this repository. Follow this file first, and use `CLAUDE.md` as the canonical detailed project guide.

## Project

- `frnk` is a Kotlin Multiplatform + Compose Multiplatform toolkit, not a standalone product app.
- Downstream apps consume it as a Git submodule/composite build via `includeBuild("../frnk")`.
- `demo-android` and `iosDemoApp` are smoke harnesses only.
- Read `docs/ARCHITECTURE.md` before cross-module architecture changes.

## Repo Shape

- All modules use flat Gradle paths (`:ui-theme`, `:ui-components`, `:ui-scaffolds`, `:data-prefs-api`, `:analytics-api`, `:analytics-impl`, etc.). The last nested paths (`:shared:backend:*`) were re-flattened to `:analytics-api`/`:analytics-impl` at restructure Stage 5 (OQ-6). `:shared-ui-atoms` (split at Stage 7 → `:ui-theme`/`:ui-components`/`:ui-scaffolds`) and `:shared-ui-api` (split at Stage 6 → `:core-mvi`/`:core-nav`/`:haptics`) were transient src-less facades, deleted at the Stage 9 coordinate flip; consumers depend on the successors directly.
- Since restructure Stage 3, module files live physically under `frnk/{core,data,ui,capabilities}/` and `demo/{shared,android-app,ios-app}/`, mapped to the flat Gradle names via `projectDir` remaps in `settings.gradle.kts`.
- There is no aggregator (restructure Stage 1): consumers depend on individual modules. `:core-di` owns `initializeFrnk(modules)`; `:ui-app` owns `FrnkAppScaffold` + `frnkUiModules()`.
- Backend/database/monetization modules use an api/impl split. Do not add SDK dependencies to `*-api` modules.

## Commands

Use targeted checks when possible:

```bash
./gradlew compileAndroidMain
./gradlew testAndroidHostTest
./gradlew :demo-android:compileDebugKotlin
./gradlew :demo-android:testDebugUnitTest
./gradlew :data-prefs-api:testAndroidHostTest
./gradlew ktlintFormat
./gradlew assemble
./gradlew :demo-shared:assembleDemoKitDebugXCFramework
```

`local.properties` is gitignored and may be required for local builds. Do not print secrets from it.

## Toolchain

- JDK 17.
- Kotlin 2.4.0, AGP 9.2.1, Gradle 9.5.1.
- AGP 9 KMP modules use `com.android.kotlin.multiplatform.library` and `kotlin { android { ... } }`, not `com.android.library` or a top-level `android {}` block.
- `demo-android` uses AGP 9 built-in Kotlin. Do not add `kotlin.android`.
- SDK versions live in `gradle/libs.versions.toml`; non-SDK constants live in `buildSrc/src/main/kotlin/ProjectConfiguration.kt`.

## Code Rules

- Make the smallest safe change and avoid unrelated edits.
- Preserve module boundaries and existing naming/style.
- Return `AppResult` from domain APIs; do not throw for expected failures.
- Keep third-party SDKs out of `*-api` modules.
- Do not add Material3 outside `:ui-bottom-nav`; the design-system modules (`:ui-theme`/`:ui-components`/`:ui-scaffolds`) stay `compose-unstyled` based.
- Prefer `MviViewModel`, `FrnkMviScreen`, `EffectCollector`, and Navigation3 helpers over hand-rolled state/effect/navigation plumbing.
- UI state should be hoisted into ViewModels. Use local Compose state only for local UI holders such as scroll, focus, animation, and remembered builders.
- New screens/features should be represented in `demo-shared`, `demo-android`, and `iosDemoApp` unless clearly inapplicable.

## Testing

- Validate with the narrowest useful Gradle task first.
- KMP host tests run with `testAndroidHostTest`, not `testDebugUnitTest`.
- `demo-android` is a normal Android app, so its tests use `testDebugUnitTest`.
- Put platform-agnostic logic tests in `commonTest`; platform runtime tests go in `androidHostTest`.
- `:ui-components` / `:ui-scaffolds` Compose UI tests live in `androidHostTest` with Robolectric (both apply the `frnk.kmp.library.composehosttest` convention plugin).

## Git Safety

- The worktree may contain user changes. Do not revert or overwrite them unless explicitly asked.
- Do not commit, push, merge, or open PRs unless explicitly requested.
- Before finishing, summarize changed files and verification performed.
