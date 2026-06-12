# frnk — Open Backlog

> The remaining open work, measured against `REQUIREMENTS.md`. The foundational
> tiers (test harness, data layer, DI, navigation, analytics/crash, monetization,
> and the full Atomic Design system) have all shipped, and the 12-stage module
> restructure is complete — only the items below are still outstanding.
>
> **How to use:** each task is sized to be completed and reviewed independently,
> with explicit Acceptance Criteria (AC) that double as the "evidence of done."
>
> **Conventions for every task** (apply unless overridden):
> - Respect all invariants in `REQUIREMENTS.md` §2 and the strict UI rules §4.
> - `*-api` modules stay SDK-free; impls bind via Koin; interfaces return
>   `AppResult`.
> - **Demo rule:** a feature is not done until exercised in `:demo-shared`,
>   `demo-android`, and `iosDemoApp` (or a written justification of why not).
> - Must pass `./gradlew compileAndroidMain` and `./gradlew testAndroidHostTest`
>   (KMP modules; `:demo-android` uses `testDebugUnitTest`); pre-commit
>   `ktlintFormat` must leave the tree clean.

---

## BUG — OnboardingScreen buttons unresponsive when pushed as a nav3 destination

**Description:** With `OnboardingScreen` pushed onto a tab's back stack (Settings → Show
Onboarding), its **buttons don't respond** — the close-X and Next/Back fire no intent — while the
`HorizontalPager` swipe and system/predictive back work fine. Discovered during the scaffold-system
device verification (2026-06-10); **reproduces identically on `main` (87aba0e)**, so it predates
`FrnkAppShell` (not a regression of the shell's `entry(ToolkitRoute.Onboarding)` registration —
verified by A/B-installing both branches on the same emulator). Repro: demo app → Settings → Show
Onboarding → tap X or Next (emulator API 36 + Pixel 7a). Suspects: the onboarding `koinViewModel`'s
intent collector vs. the nav-entry ViewModelStoreOwner, or the button taps never reaching the
composables on that destination. Workaround: pager swipe + system back.

---

## PostHog analytics tracker

**Description:** Add a PostHog `AnalyticsTracker` implementation as the
provider-neutral analytics option named in `REQUIREMENTS.md` §3.6.
**Rationale:** Named requirement; rounds out analytics (Firebase analytics/crash
already ship) without coupling product analytics to a backend choice.
**Scope:** likely a small new impl module (e.g. `analytics-posthog`) or an
addition under the backend-agnostic analytics path — decide and record the
decision; keep the api SDK-free.
**Acceptance Criteria:**
- [ ] `AnalyticsTracker` implemented against PostHog; bound via its own Koin
      module; installable independently of the data backend.
- [ ] `noopObservabilityModule` remains the safe default.
- [ ] Demoed (an event fired from `DemoScreen`, visible in logs/fake in demo).
