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
fun bootstrapFrnkKit(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
): KoinApplication
```

From Swift: `FrnkKitKt.bootstrapFrnkKit(backend: .supabase, observability: .firebase)`. Add new top-level Kotlin functions to this file if iOS needs a thinner / more Swift-friendly facade than `initializeFrnk` directly.

Selecting `observability: .firebase` installs Firebase Analytics + Crashlytics **and** the CrashKiOS unhandled-Kotlin-exception hook (so uncaught Kotlin crashes reach Crashlytics symbolicated). The consumer must supply the Firebase Crashlytics pod, call `FirebaseApp.configure()`, and upload dSYMs (see below).

## Crashlytics setup for consumer apps (do this for every new iOS app)

> **Claude: when the user is creating/scaffolding a new iOS app that consumes FrnkKit and wants
> crash reporting, walk them through this checklist.** The toolkit ships a prebuilt static
> framework and **cannot** wire any of this for them — it's per-app Xcode setup. Skipping step 4
> is the #1 reason "crashes don't show up in Crashlytics."

Why it's needed: `FrnkKit.xcframework` is `isStatic = true`, so frnk's Kotlin symbols are linked
into the **consumer app binary** at the app's link step. Selecting `observability: .firebase`
installs the CrashKiOS hook that turns an uncaught Kotlin exception into a Crashlytics report — but
Crashlytics still needs the matching **dSYM** uploaded to symbolicate it.

Per-app checklist:
1. **Add Firebase** to the Xcode project — Firebase Apple SDK via SPM (`FirebaseCrashlytics` product;
   pulls `FirebaseCore`/`FirebaseAnalytics`) or CocoaPods — and add the app's `GoogleService-Info.plist`
   to the target.
2. **Configure + install the hook** in Swift, early at launch:
   ```swift
   FirebaseApp.configure()
   _ = FrnkKitKt.bootstrapFrnkKit(backend: .supabase, observability: .firebase) // installs CrashKiOS hook
   ```
3. **Confirm Release builds emit dSYMs** — `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` (Xcode's
   Release default; Debug defaults to `dwarf` = **no dSYM**, so only Release/Archive symbolicates
   out of the box).
4. **Upload dSYMs to Crashlytics** — add the Crashlytics **run-script build phase** so every archive
   uploads automatically (SPM path):
   ```
   "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
   ```
   (`iosDemoApp` has a working example of this build phase — copy its shape.)

KMP specifics that save you work:
- **One dSYM covers everything.** Because FrnkKit is **static**, your app's own dSYM already contains
  frnk's Kotlin frames — the standard app-dSYM upload symbolicates both your Swift and frnk's Kotlin.
  **No separate Kotlin-framework dSYM step**, and **no** CrashKiOS `crashlyticslink` Gradle plugin
  (that's only for *dynamic* frameworks).
- **Production needs no per-release manual step** once step 4's build phase exists. The manual
  `upload-symbols -gsp GoogleService-Info.plist -p ios <App.xcarchive/dSYMs>` (or Xcode Organizer →
  App Store Connect dSYM download) is only a fallback if the build phase is missing or App Store
  re-thins the binary.

Gotcha we hit (so you don't again): a crash showing as **"unprocessed — upload 1 dSYM file"** means
the report arrived but no matching dSYM was uploaded — usually a **Debug** build (no dSYM generated)
or a missing run-script. Crashes upload on the **next launch**, and the **first-ever** crash can take
several minutes to surface. Details in `shared/shared-backend-firebase/CLAUDE.md`.

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
