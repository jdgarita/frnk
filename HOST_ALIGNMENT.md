# HOST_ALIGNMENT.md

How a production host app — **Still**, and every future app — integrates the `frnk` toolkit, inherits its
build configuration, extends its DI graph, overrides its design tokens, supplies a custom icon pack, and
follows its component conventions.

`frnk` is a Kotlin Multiplatform + Compose Multiplatform **toolkit**, consumed as a Git submodule via a
Gradle composite build. The host owns the application; `frnk` provides the engine, design system, and
backend/monetization plumbing. See `docs/ARCHITECTURE.md` for the module graph.

---

## 1. Integrate frnk

Add the toolkit as a submodule and wire the composite build:

```bash
git submodule add <frnk-repo-url> frnk
```

```kotlin
// host settings.gradle.kts
pluginManagement {
    includeBuild("frnk")          // composite build — the toolkit builds alongside the host
}
```

```kotlin
// Android: host app/build.gradle.kts
dependencies {
    implementation("dev.jdgarita.frnk:androidApp")   // re-exports :shared (the whole toolkit surface)
}
```

iOS: build `FrnkKit.xcframework` (`./gradlew :iosApp:assembleFrnkKitReleaseXCFramework`) and embed it; the
consumer supplies the native Firebase / RevenueCat SDKs via SPM/CocoaPods under the toolkit's
`-undefined dynamic_lookup` link (see `iosApp/CLAUDE.md`).

---

## 2. Inherit build configuration (single source of truth)

The host **does not** redeclare SDK targets or library versions — it inherits them from frnk.

**Version catalog.** A composite build does not auto-share `gradle/libs.versions.toml`, so import it
explicitly under a distinct name:

```kotlin
// host settings.gradle.kts
dependencyResolutionManagement {
    versionCatalogs {
        create("frnkLibs") { from(files("frnk/gradle/libs.versions.toml")) }
    }
}
```

Now the host references the same pinned versions frnk uses — e.g. `implementation(frnkLibs.koin.core)`,
`alias(frnkLibs.plugins.kotlin.multiplatform)`.

**SDK targets.** `min/compile/targetSdk` are the **single source of truth** in
`frnk/gradle/libs.versions.toml` (`android-minSdk` / `android-compileSdk` / `android-targetSdk`) — read
by frnk's own modules and inherited by hosts via the catalog:

```kotlin
// host android block
compileSdk = frnkLibs.versions.android.compileSdk.get().toInt()
minSdk     = frnkLibs.versions.android.minSdk.get().toInt()
```

**Convention plugin (optional).** frnk's standard KMP modules apply the `frnk.kmp.library` convention
plugin from its `build-logic` included build (jvmToolchain 17 + Android SDK + iOS framework in one line).
A host that adds its own KMP library modules can apply the same plugin by adding
`includeBuild("frnk/build-logic")` to its `pluginManagement` and `plugins { id("frnk.kmp.library") }`.

---

## 3. Bootstrap + contribute custom Koin modules

frnk assembles its Koin graph from three independent axes — **backend** (`BackendChoice`), **observability**
(`ObservabilityChoice`), **monetization** (`MonetizationChoice`) — plus the host's own modules.

**Android** (`Application.onCreate`):

```kotlin
class StillApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFrnk(
            backend = BackendChoice.Supabase,            // or .Firebase
            observability = ObservabilityChoice.Firebase, // independent of backend; .None by default
            monetization = MonetizationChoice.RevenueCat, // or .None to opt out of billing
            additionalModules = listOf(stillModule),      // ← the host's own Koin modules, first-class
        ) {
            androidContext(this@StillApp)                 // extraConfig: Koin DSL escape hatch
            // allowOverride(true)  // if a host module overrides a toolkit binding
        }
    }
}
```

- **`additionalModules`** is the first-class way to register host repositories, feature ViewModels, a custom
  `SqlDriver` schema, or a custom `EntitlementProvider`. They install **after** the toolkit's, so with
  `allowOverride(true)` a host can override a toolkit binding.
- **`extraConfig`** is the lambda for everything else on the `KoinApplication` (`androidContext(...)`, logging).
- **Monetization opt-out:** `MonetizationChoice.None` installs **no** billing bindings. A host using a
  different provider passes its own `EntitlementProvider` via `additionalModules` (optionally with the
  toolkit's `monetizationModule` / `paywallScaffoldModule` over it).

**iOS** (Swift, on launch):

```swift
FrnkKitKt.bootstrapFrnkKit(
    backend: .supabase,
    observability: .firebase,
    monetization: .revenueCat,
    additionalModules: [/* host Koin modules */]
)
```

---

## 4. Override design tokens

Every design axis has defaults and is host-overridable through the single `FrnkThemeConfig` entry point —
**colors, typography, dimens/padding (spacing), shapes, icon sizes, strings, and icons** — wrapped once at
the app root:

```kotlin
FrnkTheme(
    config = FrnkThemeConfig(
        lightColorOverrides = mapOf(colorPrimary to Still.Brand, colorPrimaryContainer to Still.BrandTint),
        darkColorOverrides  = mapOf(colorPrimary to Still.BrandDark),
        textStyleOverrides  = mapOf(titleLarge to Still.Title),
        fontFamily          = Still.Inter,                 // restyle ALL text styles at once
        shapeOverrides      = mapOf(shapeButton to RoundedCornerShape(8.dp)),
        spacingOverrides    = mapOf(spacingMd to 12.dp),   // dimens/padding axis (host-alignment refactor)
        iconSizeOverrides   = mapOf(iconSizeMd to 28.dp),  // icon-size axis
        stringOverrides     = mapOf(stringUpgrade to "Go Pro"),
        iconOverrides       = mapOf(iconBack to Still.Icons.Back),
    ),
) {
    StillApp()
}
```

Overrides merge on top of the bundled defaults per token (`Map.plus` — host values win); supply only the
tokens you want to change. Atoms read every value through `Theme[...]` (e.g. `Theme[spacing][spacingMd]`),
so overrides take effect everywhere without forking the toolkit.

---

## 5. Supply a custom icon pack

The toolkit ships a default Lucide-backed icon registry (`iconBack`, `iconClose`, `iconSearch`,
`iconSettings`, …). A host overrides any or all of them — **or adds brand-specific icons** — through
`iconOverrides`:

```kotlin
FrnkThemeConfig(
    iconOverrides = mapOf(
        iconBack      to StillIcons.ChevronLeft,   // override a generic default with a brand glyph
        iconSettings  to StillIcons.Gear,
        iconUpgrade   to StillIcons.Crown,
        // brand-only token the host declares itself:
        stillRocket   to StillIcons.Rocket,
    ),
)
```

`Theme[icons][token]` resolves overrides transparently at every call site — atoms don't know or care whether
an icon is a Lucide default or a host glyph. Hosts that never reference Lucide icons by name don't take the
Lucide dependency at all.

---

## 6. Component style guide — sealed state + `Skeleton` object

**Every frnk UI component with multiple visual states models its state as a `sealed interface` with a
`Content` data class and a `data object Skeleton`** (and an `Error` data class where there's a real error
visual). The composable `when`-switches; the `Skeleton` branch is a **non-interactive** token-driven
placeholder. Copy this shape for new components (host or toolkit):

```kotlin
sealed interface FrnkButtonState {
    @Immutable
    data class Content(
        val text: String,
        val enabled: Boolean = true,
        val variant: FrnkButtonVariant = FrnkButtonVariant.Filled,
    ) : FrnkButtonState

    data object Skeleton : FrnkButtonState
    // data class Error(val message: String) : FrnkButtonState   // when the component shows errors
}

@Composable
fun FrnkButton(state: FrnkButtonState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        is FrnkButtonState.Content -> { /* interactive button, styled from Theme[...] tokens */ }
        FrnkButtonState.Skeleton  ->
            Box(modifier.frnkSkeleton(FrnkSkeleton(enabled = true), shape = shapeButton)) // inert placeholder
    }
}
```

Rules: state is hoisted into the feature's `MviViewModel` (never `remember { mutableStateOf }` for
screen/business state); styling comes from `Theme[colors|textStyles|shapes|spacing|iconSizes][token]`, never
hardcoded `Color(0xFF…)` / raw `.dp`; the `Skeleton` branch renders no clickable/toggleable node.
Single-state or terminal components (a pure divider, a terminal empty state, interaction-only chrome) stay
non-sealed and skip the `Skeleton` object — note why. `FrnkText` additionally keeps a per-subtype `skeleton`
field for content-sized text skeletons. See `shared/shared-ui-atoms/CLAUDE.md` for the full convention.

---

## 7. Demo isolation & the upstream rule

`androidDemoApp` / `iosDemoApp` are **pure host harnesses**: they only initialize frnk, provide config
overrides, and showcase default behavior — they contain **zero** reusable library logic (the shared demo UI
itself lives in `:shared-demo`, consumed by both). Treat them as disposable references for how a host wires
the toolkit.

**The upstream rule:** anything reusable belongs **inside** a `frnk` `shared-*` module, never at the app
layer. When building a feature in a host, ask whether it can be abstracted upstream into the toolkit (a new
atom, a backend interface + impl pair, a scaffold) so every future host inherits it. If something genuinely
must live at the app level (a host-only screen, brand assets), keep it there — but the default is upstream.
