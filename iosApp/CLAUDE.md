# iosApp

iOS consumer entry point. Produces the **`FrnkKit.xcframework`** that downstream iOS apps embed.

## What it does

- Declares the three Apple targets (`iosX64`, `iosArm64`, `iosSimulatorArm64`) and bundles them into a single XCFramework via `XCFramework(ProjectConfiguration.IOS_FRAMEWORK_NAME)`.
- Each framework binary `export`s `projects.shared`, so the whole toolkit surface (atoms, MVI, backend interfaces, monetization, …) is callable from Swift through one framework.
- `isStatic = true`.
- `linkerOpts("-undefined", "dynamic_lookup")` — **deliberate**. Bundled impls (`shared-monetization-revenuecat`, `shared-backend-firebase`) reference native iOS frameworks (`PurchasesHybridCommon`, Firebase pods) that the toolkit does **not** ship. Deferring symbol resolution makes the XCFramework link locally; the consumer Xcode project's link step resolves the symbols via CocoaPods / SPM.

## Public Swift surface

`iosApp/src/iosMain/kotlin/dev/jdgarita/frnk/ios/FrnkKit.kt`:

```kotlin
fun bootstrapFrnkKit(backend: BackendChoice = BackendChoice.Supabase): KoinApplication
```

From Swift: `FrnkKitKt.bootstrapFrnkKit(backend: .supabase)`. Add new top-level Kotlin functions to this file if iOS needs a thinner / more Swift-friendly facade than `initializeFrnk` directly.

## Build

```bash
./gradlew :iosApp:assembleFrnkKitReleaseXCFramework
# → iosApp/build/XCFrameworks/release/FrnkKit.xcframework
```

The framework base name comes from `ProjectConfiguration.IOS_FRAMEWORK_NAME` (`FrnkKit`). Keep it stable — renaming it is a breaking change for every consumer Xcode project.

## Rules

- **Do not** put concrete features here. Anything callable from Swift must live in a `shared-*` module that `:shared` `api`-depends on; this module only re-exports.
- **Do not** add `linkerOpts` for specific frameworks (e.g. `-framework FirebaseAuth`). The "dynamic_lookup" approach is intentional so consumers retain full control of the native dep list.
- New `expect`/`actual` for iOS goes in the module that owns the feature, not here. This module is a packaging shim.
- No Android target — `androidApp` is the Android-side mirror.
