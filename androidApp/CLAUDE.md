# androidApp

The Android consumer entry point for the toolkit. **This is a KMP-Android library, not an application** — `androidApp/build.gradle.kts` applies `alias(libs.plugins.android.kotlin.multiplatform.library)` (AGP 9's `com.android.kotlin.multiplatform.library`), not `application`. The shipping product is this library; the Android smoke app is `:androidDemoApp`.

## Purpose

A one-line re-export of `:shared` so downstream apps depend on a single artifact:

```kotlin
sourceSets.androidMain.dependencies {
    api(projects.shared)
}
```

Downstream apps declare:

```kotlin
implementation("dev.jdgarita.frnk:androidApp")
```

…and get the full toolkit surface transitively. Hosts then call `initializeFrnk(backend = ...)` from their `Application.onCreate()`.

## Rules

- **Do not add code here.** No Activities, no `Application` subclass, no Compose. If you find yourself reaching for `androidx.activity.compose`, you want `:androidDemoApp` instead.
- **Do not add dependencies** other than the `api(projects.shared)` re-export. The toolkit's whole point is that `:shared` aggregates everything; this module just exposes it under an Android coordinate.
- This module has only `androidMain` configured — `iosApp` is the iOS-side equivalent. Don't add iOS targets here.
- Namespace is `${ProjectConfiguration.GROUP_ID}.android`. Don't change without coordinating with downstream consumers' Gradle imports.

## Why it exists

Composite-build consumers can `implementation(projects.sharedXxx)` to get individual modules, but in practice they want "the toolkit" as one dep. `:androidApp` is that one dep on Android.
