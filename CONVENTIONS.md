# Conventions

Coding rules for `frnk`. `CLAUDE.md` holds the long-form "how to add X" recipes; this file is the
short list of rules every change is reviewed against. `ARCHITECTURE.md` explains the layering.

## 1. Dependency injection

- **Prefer Kotlin `interface` + Koin binding over `expect`/`actual`.** Declare the contract in
  `commonMain`, implement it per platform (or per SDK) in a separate class, bind it in a Koin
  module. This keeps `commonMain` compilable and testable with a fake, and lets a host swap the
  implementation without a source-set change.
- `expect`/`actual` is acceptable only for tiny leaf primitives with no dependencies and no
  behaviour worth faking (a device-model string, a platform URL, a platform-native composable).
- Every module exports its bindings as a top-level `val xxxModule = module { … }`. Hosts compose
  the graph explicitly via `initializeFrnk(modules = …)`; there is no auto-discovery and no enum
  of "backends".
- Resolve dependencies through constructors. No service locator calls (`get()`, `inject()`)
  inside domain or ViewModel code; only Compose entry points use `koinViewModel` / `koinInject`.
- A `*-api` module never depends on a third-party SDK. Toolkit code never imports an `*-impl`
  package. Demo modules are the only exception.

## 2. UI

- **No Material 2/3 and no `com.composables:core`.** The design system is built on
  `compose-unstyled` (`composeunstyled-*` + `icons-lucide-cmp`). The single sanctioned Material3
  dependency is `:ui-bottom-nav`; do not add Material3 anywhere else, and do not introduce it into a
  host unless that host already explicitly uses it.
- Style from tokens only: `Theme[colors][…]`, `Theme[textStyles][…]`, `Theme[spacing][…]`,
  `Theme[iconSizes][…]`. Never hardcode `Color(0xFF…)` or raw `.dp`.
- Atoms/molecules/organisms take an `@Immutable *State` (a `sealed interface` with `Content` +
  `Skeleton`, plus `Error` where a real error visual exists), then callbacks, then `modifier`.
  Decide explicitly whether a new component needs a loading skeleton and record why if not.
- Ripple and haptics are installed by `FrnkTheme`; do not re-wire them per component.
- Composables default to `internal` (including `*Content` renderers) unless an out-of-module
  consumer needs them.
- `@Preview`s live in `src/commonDebug/…/previews/`.

## 3. State and immutability

- **`val` by default.** `var` only for genuinely mutable local holders, and never in a public API.
- Models, UI states, intents, effects, and route keys are `data class` / `data object` /
  `sealed interface`. Use `copy(...)` to produce the next state; never mutate in place.
- Collections in state are read-only (`List`, `Map`, `Set`). Wrap them as `@Immutable` or
  `@Stable` state classes so Compose can skip.
- **Hoist state.** Screen, navigation, and business state lives in an `MviViewModel`
  (`updateModel { copy(...) }` → pure `mapToUiState`). `remember` / `rememberSaveable` are for
  local UI holders only (scroll, focus, animation, `remember`-built helpers).
- ViewModels expose a single `StateFlow<UiState>` plus a one-shot effect stream. Collect effects in
  exactly one place (`FrnkScreen`'s `onEffect`); the channel is single-consumer.

## 4. Errors

- **Return, don't throw.** Every `*-api` interface and every use case returns
  `AppResult<D, E : AppError>` (`Success` / `Failure`, in `:shared-utils`). Errors are `sealed`
  hierarchies implementing `AppError`, so callers `when` over them exhaustively.
- Use `kotlin.Result` only at SDK boundaries inside an impl (`runCatching { sdk.call() }`), then map
  it to `AppResult` before it leaves the module. Never surface `Throwable` in a public signature.
- Never catch `CancellationException`, `Throwable`, or `Exception` broadly around suspend calls;
  catch the specific failure, or use `runCatching` at the SDK edge only.
- Best-effort side effects (telemetry, crash reporting) discard their `AppResult`; gating operations
  (billing, persistence) propagate it. Do not "fix" a best-effort sink into a blocking one.

## 5. Coroutines

- **Structured concurrency, bounded scopes.** Launch from `viewModelScope`, `LaunchedEffect`,
  `rememberCoroutineScope`, or a scope the caller injects. No `GlobalScope`, no
  `CoroutineScope(Dispatchers.X)` fields that nobody cancels, no `runBlocking` outside tests.
- Suspend functions are **main-safe**: the callee switches dispatchers (`withContext(io)`), the
  caller never does. Inject dispatchers so tests can replace them.
- Prefer cold `Flow` for streams and `StateFlow` for observable state; expose read-only types.
- Non-suspending APIs never start coroutines in `init`.

## 6. Platform boundaries

- **No platform APIs in `commonMain`.** No `android.*`, `androidx.*` beyond the KMP-safe
  `lifecycle` / `navigation3` / `savedstate` artifacts, no `platform.*` (UIKit/Foundation), no JVM
  `java.*`. If it needs a `Context`, a `UIViewController`, or a `Vibrator`, it goes behind an
  interface bound in `androidMain` / `iosMain`.
- Platform source sets contain only bindings and adapters. Business logic in `androidMain` or
  `iosMain` is a bug.
- Anything Swift must call lives in a module the umbrella framework `export(...)`s, uses
  Swift-friendly types, and avoids native cinterop in `commonMain` (keeps XCFrameworks clean).
- Firebase, RevenueCat, SQLite drivers, and any other native SDK are confined to `*-impl` modules
  and the platform wrappers.

## 7. Code organisation and style

- **Extension functions live in their own file** under an `ext/` subpackage, named `<Type>Ext.kt`,
  never appended to the type's declaration file.
- One public type per file; file name matches the type.
- Package = `dev.jdgarita.frnk.<area>`; the Maven group id comes from the version catalog
  (`frnk-groupId`), never hardcoded.
- **No trailing commas, no final newline in Kotlin** (`.editorconfig` + ktlint, enforced by the
  pre-commit hook). Run `./gradlew ktlintFormat` rather than hand-formatting.
- Analytics `ToolkitEvent` keys are lowercase `snake_case`, letters/digits/underscores only,
  starting with a letter (Firebase silently drops anything else).
- New typed preferences use `Preference<T>` (`store.booleanPreference("key", default)`), never raw
  `KeyValueStore` keys.

## 8. Tests

- Reducers and `*-api` logic land with tests in `commonTest`. Platform-only tests
  (Compose UI + Robolectric, JDBC driver) go in `androidHostTest`.
- Fakes for `*-api` interfaces live in the api module's `commonTest` (pattern:
  `FakeAnalyticsTracker`); tests never import an `*-impl`.
- ViewModel tests set `Dispatchers.setMain` and follow `MviViewModelTest` in `:core-mvi`.
- Design-system components are previews-only except the highest-value atoms, which have Compose UI
  tests in `androidHostTest`.

## 9. Definition of done

1. Compile gate passes: `./gradlew compileAndroidMain :demo-android:compileDebugKotlin`.
2. Test gate passes: `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest`.
3. The feature is exercised in `:demo-shared`, `demo-android`, and `iosDemoApp`, or the reason it
   cannot be is written down.
4. Any non-obvious decision or workaround is saved to the MobiAI brain, not to the docs.
