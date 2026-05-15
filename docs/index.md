---
layout: default
title: frnk
description: A Kotlin Multiplatform + Compose Multiplatform toolkit for indie apps.
---

<p align="center">
  <img width="180" alt="frnk logo" src="https://github.com/user-attachments/assets/dc37abec-66be-4754-ad36-a92093c91e0b" />
</p>

# frnk

**A Kotlin Multiplatform + Compose Multiplatform toolkit — not a standalone app.**

`frnk` gives indie and small-team apps a fast-compiling, strictly-modular baseline so day-to-day work stays on features instead of plumbing. It's consumed as a Git submodule via a Gradle composite build — one dependency, one Koin bootstrap call, runtime choice of backend.

[View on GitHub](https://github.com/jdgarita/frnk){: .btn }
[Architecture](https://github.com/jdgarita/frnk/blob/main/docs/ARCHITECTURE.md){: .btn }
[Releases](https://github.com/jdgarita/frnk/releases){: .btn }

---

## What's in the box

- **MVI presentation layer** — `MviViewModel<S, A, E>`, `ObserveAsEvents`, and a pure-reducer pattern shared across Android and iOS.
- **Headless Compose UI atoms** built on [`compose-unstyled`](https://github.com/composablehorizons/compose-unstyled).
- **Swappable backends** — Firebase **and** Supabase implementations are bundled; the host picks one at runtime via `BackendChoice`.
- **Persistence** — SQLDelight + Multiplatform Settings, behind a `*-api` interface so impls stay swappable.
- **Monetization** — RevenueCat entitlements behind a `FeatureGate` interface, with a fake for demos and tests.
- **Strict api / impl module split** — third-party SDKs never leak into interface modules, so parallel compilation stays fast.

## Tech

Kotlin 2.3.21 · Compose Multiplatform 1.10.3 · Koin 4.2.1 · SQLDelight 2.3.2 · Ktor 3.5.0 · Supabase 3.6.0 · GitLive Firebase 2.4.0 · RevenueCat 3.0.1 · AGP 9.2.1 · Gradle 9.5.1 · JDK 17.

## Consume it

Add as a submodule, pin to a release tag, wire into the consumer's `settings.gradle.kts` as a composite build:

```kotlin
// settings.gradle.kts (consumer app)
pluginManagement {
    includeBuild("frnk")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
```

```kotlin
// app/build.gradle.kts (consumer app)
dependencies {
    implementation("dev.jdgarita.frnk:androidApp")
}
```

```kotlin
// Application.onCreate
initializeFrnk(backend = BackendChoice.Supabase) {
    androidContext(this@MyApp)
    modules(myAppModule, sqlDelightSchemaModule)
}
```

For iOS, `./gradlew :iosApp:assembleFrnkKitReleaseXCFramework` produces `FrnkKit.xcframework` for SPM consumption; Swift calls `FrnkKitKt.bootstrapFrnkKit(backend:)`.

Full integration guide: [`docs/HOST_INTEGRATION.md`](https://github.com/jdgarita/frnk/blob/main/docs/HOST_INTEGRATION.md).

## Documentation

- [Architecture](https://github.com/jdgarita/frnk/blob/main/docs/ARCHITECTURE.md) — module graph, api/impl rationale, `:shared` aggregation strategy.
- [Host integration](https://github.com/jdgarita/frnk/blob/main/docs/HOST_INTEGRATION.md) — step-by-step consumer setup.
- [Releasing](https://github.com/jdgarita/frnk/blob/main/docs/RELEASING.md) — maintainer release procedure.
- [Changelog](https://github.com/jdgarita/frnk/blob/main/CHANGELOG.md) — version history.
