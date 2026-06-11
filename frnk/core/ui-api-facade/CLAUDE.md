# shared-ui-api (facade)

**Transient facade — no source.** `:shared-ui-api` was split at restructure Stage 6 into `:core-mvi`
(MVI engine + `UiText`), `:core-nav` (Compose-free Nav3 contract) and `:haptics` (haptics contract).

This module exists only to keep the `dev.jdgarita.frnk:shared-ui-api` coordinate resolving for
downstream consumers (the still host app + any internal module that hasn't re-pointed) until the
coordinate flip at **Stage 9**, when it is deleted. Its `build.gradle.kts` is just
`api(projects.coreMvi); api(projects.coreNav); api(projects.haptics)` — `api()` re-exports the three
successors so the transitive classpath is byte-for-byte what `shared-ui-api` advertised before the split.

Parked at `frnk/core/ui-api-facade` (not `frnk/core/mvi` — that directory is now `:core-mvi`; two
Gradle projects can't share a `projectDir`).

Do not add source or dependencies here. New MVI/nav/haptics code goes in the successor modules.
