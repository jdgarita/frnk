# Testing Patterns

<!--
Reusable testing patterns discovered for this project.
Append entries with: mobiai brain save testing (coming in Phase 2).
Include the problem, the pattern that solved it and a minimal example.
-->

## frnk test conventions: testAndroidHostTest, source-set placement, Robolectric Compose UI, *-api fakes

- id: frnk-test-conventions-testandroidhosttest-source-set-placeme-20260612-030809
- type: testing_pattern
- status: active
- platform: kmp
- area: testing
- date: 2026-06-12

The reusable testing patterns for this toolkit (migrated from REQUIREMENTS.md §5 / README CI section so they live where the agent looks before writing tests).

### Host-test task is testAndroidHostTest (NOT testDebugUnitTest)
KMP-Android modules (`com.android.kotlin.multiplatform.library`) have **no** `testDebugUnitTest`. A module only gets a host unit-test compilation after opting in with `kotlin { android { withHostTest {} } }` + adding `commonTest.dependencies { implementation(libs.kotlin.test); implementation(libs.kotlinx.coroutines.test) }`. Run with **`testAndroidHostTest`** (single module: `:data-prefs-api:testAndroidHostTest`). Only `:demo-android` (a `com.android.application`) uses `testDebugUnitTest`.

### Source-set placement
- Platform-agnostic logic (reducers, `*-api` logic) → the module's `commonTest` source set.
- Tests needing a platform-only runtime (Compose UI + Robolectric, the JDBC driver) → `androidHostTest`.
- `:ui-components` / `:ui-scaffolds` have **no** `commonTest` — all their tests are `androidHostTest` (they apply `frnk.kmp.library.composehosttest`, which supplies `withHostTest{isIncludeAndroidResources}` + the Robolectric bundle + the commonDebug preview wiring).

### Reducer/ViewModel tests
Follow `MviViewModelTest` in `:core-mvi`: set `Dispatchers.setMain(...)` since `viewModelScope` drives the intent collector. Reducers + `*-api` logic should land with tests.

### *-api fakes
Follow `FakeAnalyticsTracker` / `FakeCrashReporter` in `:analytics-api`'s `commonTest` — the canonical hand-written fake pattern for `*-api` interfaces (no real SDK in test sources).

### Design-system Compose UI tests (the exception)
Highest-value atoms only (`FrnkSwitch`, `FrnkSegmentedControl`, `FrnkTopAppBar` search mode). They need a real composition + Robolectric (no common/iOS variant), so they live in `:ui-components`'s `androidHostTest`, extend `RobolectricComposeTest` (+ `setFrnkContent { }`), drive `runComposeUiTest`, and assert the semantics tree. Run under Robolectric `GraphicsMode.LEGACY` (no device). Molecules/organisms stay previews-only.

### Local validation gate (CI is paused on the private repo)
`./gradlew compileAndroidMain :demo-android:compileDebugKotlin --parallel --build-cache` then `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest --parallel --build-cache`. Style is the pre-commit ktlint hook, not CI.
