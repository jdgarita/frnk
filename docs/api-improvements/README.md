# frnk public-API improvements

A tracked backlog of improvements and simplifications for frnk's **public API** — the surface the demo and
future host apps consume. It exists to make the toolkit easier and safer to integrate.

This backlog is **not** the canonical API reference. The canonical reference stays in the per-module
`CLAUDE.md` files + `docs/` (`ARCHITECTURE.md`, `HOST_INTEGRATION.md`); the *why* behind decisions lives in
the MobiAI brain (`.mobiai/brain/`). These docs only track candidate work, grouped by tier.

## Tiers

| Tier | Theme | Doc |
|---|---|---|
| 1 | Hygiene / honesty (fast, safe, mostly docs + dead-code) | [tier-1.md](tier-1.md) |
| 2 | Host ergonomics (the high-value simplifications) | [tier-2.md](tier-2.md) |
| 3 | Consistency & type-safety (polish) | [tier-3.md](tier-3.md) |
| 4 | Capability maturity (larger / future) | [tier-4.md](tier-4.md) |

## Status legend

- **Proposed** — identified, not yet scheduled.
- **In progress** — actively being worked.
- **Done** — landed (link the commit when shipped).
- **Deferred** — intentionally parked.

Each item records: **Problem** (grounded, with file paths) · **Proposed change** · **Host benefit** ·
**Effort** · **Risk / blast radius** · **Doc-only vs API** · **Status**.

## Strengths to preserve (don't "simplify" away)

- The explicit-module-list DI in `initializeFrnk(modules = …)` — no magic capability enum.
- The api/impl split with safe no-op defaults for every capability.
- `AppResult<D, E>` as the toolkit-wide result envelope (no throwing across `*-api` boundaries).
- Compose-free token refs (`FrnkStringSource` / `FrnkIconSource`) so ViewModels author UI state.
- The sealed-state + `Skeleton` component discipline.

## Reusable evaluation prompt

Run this to (re-)evaluate the public API in the future:

> Evaluate the public API surface of the frnk toolkit for host ergonomics, consistency, and simplification,
> treating the toolkit as a product whose users are (a) the in-repo demo and (b) future host apps integrating
> via individual module coordinates.
>
> 1. **Inventory** every public (non-`internal`) host-facing entry point, grouped by journey: *bootstrap/DI*
>    (`initializeFrnk`, `requireFrnkKoin`, the installable Koin modules), *app root* (`FrnkApp`,
>    `frnkUiModules`), *navigation* (`FrnkRoute`/`FrnkRootRoute`, nav configs, back-stack helpers,
>    `FrnkNavDisplay`, `FrnkNestedNavScaffold`/`FrnkCustomTab`), *MVI/Compose bindings* (`FrnkScreen`,
>    `MviViewModel`), *design system* (`FrnkTheme`/`FrnkThemeConfig`, atoms/molecules/organisms + their
>    `*State` shapes), and *capability/data APIs* (`KeyValueStore`/`Preference`, `SqlDriverFactory`,
>    analytics, monetization, remote-config, camera/permissions, `AppResult`).
> 2. For each, quote the **exact signature** and judge it against: least-surprise, consistency (naming, param
>    order, value-vs-lambda, state shape), required-vs-optional clarity, repeated boilerplate, footguns
>    (silent misconfig, easy-to-forget steps), and doc-vs-reality drift.
> 3. **Walk the demo as the reference host** and the `HOST_INTEGRATION.md` steps end-to-end; flag every place
>    a host copies boilerplate or must "remember" a manual step.
> 4. Output a **prioritized list of improvements**, each with: the concrete change, the host benefit, rough
>    effort, risk/blast-radius, and whether it's a doc-only or API change. Prefer **additive, non-breaking**
>    simplifications; call out anything that would break the demo or existing hosts.
> 5. **Verify** any "this doesn't exist / is unused" claim by grepping the source before asserting it.
