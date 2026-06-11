# shared-ui-atoms (facade)

**Transient src-less facade** (restructure Stage 7b). `shared-ui-atoms` was split into `:ui-theme`
(tokens + theme engine), `:ui-components` (atoms/molecules/organisms/placeholder) and `:ui-scaffolds`
(scaffolds + Compose MVI/Nav bindings). This module keeps the old `dev.jdgarita.frnk:shared-ui-atoms`
coordinate alive — **no source of its own** — by `api()`-re-exporting the four successors
(`:ui-theme`, `:ui-components`, `:ui-scaffolds`, `:haptics`), preserving the exact transitive classpath
the module advertised before the split.

- Parked at `frnk/ui/atoms-facade` (NOT `frnk/ui/components` — that directory is now `:ui-components`;
  two Gradle projects can't share a `projectDir`), remapped in `settings.gradle.kts`.
- **Deleted at restructure Stage 9** (the coordinate flip), once still re-points to the new coordinates.
- `:shared-ui-nav` keeps depending on this facade (`api(projects.sharedUiAtoms)`) as the
  **still-invisibility proof** — an internal consumer whose classpath flows through the facade
  unchanged. `demo/shared` and `:shared-monetization-ui` re-pointed to the successors directly (the demo
  because Kotlin/Native `export` is non-transitive; monetization-ui to reach the Stage 8 precondition
  `{ui-scaffolds, monetization-api}`).

Applies only `frnk.kmp.library` — it compiles no Compose code, so it needs no Compose plugin; the
transitive `api` exports flow through regardless.
