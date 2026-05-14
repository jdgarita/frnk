# frnk

A Kotlin Multiplatform + Compose Multiplatform toolkit consumed by downstream
apps as a Git submodule via Gradle composite builds.

See `docs/ARCHITECTURE.md` for the full architectural overview, including the
api/impl module pattern, MVI engine, and how to integrate this library into
an external project via `includeBuild()`.

## Quickstart

```bash
cp local.properties.template local.properties
# Fill in Supabase / Firebase keys
./gradlew help
./gradlew :core-network-impl:allTests
```

## Module map

- `core-common` — `Result` wrapper, `UiText`, primitives shared across api modules.
- `core-network-api` / `core-network-impl` — Networking interfaces & Ktor impl.
- `core-database-api` / `core-database-impl` — Storage interfaces & SQLDelight impl.
- `core-ui-atoms` — Headless Compose components (compose-unstyled) + MVI engine.
- `androidApp` (library) / `iosApp` (framework) — public entry points.
- `androidDemoApp` / `iosDemoApp` — internal test harnesses.
