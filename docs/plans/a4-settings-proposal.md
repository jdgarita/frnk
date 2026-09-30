# A4: Faint's Settings as configuration of `FrnkSettingsScreen` (proposal)

> **Status:** proposal, not implemented. Plan item **A4** of Still's
> `docs/plans/2026-09-29-frnk-upstream-from-faint.md` ("moving Faint's pieces into frnk"). A3
> (`FrnkDialog`, `NoSubscriptionFoundDialog`) and A5 (the persisted haptics switch) landed on the same
> branch and are prerequisites. Decisions below are recommendations; the open questions at the end
> need an answer before implementation starts.

## Goal

Faint's Settings screen (and Still's, which replicates it: Still `docs/04-screen-specs.md` §5) must be
expressible as **configuration of frnk's existing settings scaffold**. There must be **one** settings
system in frnk: one state/intent/effect vocabulary, one renderer (`FrnkSettingsScreen`), one
ViewModel. No second "Faint-style" settings screen next to it.

Hard constraints:

- `:ui-scaffolds` stays SDK-free. Anything that talks to `EntitlementManager`, `SyncAuthUseCase` or
  store URLs lives in `:shared-monetization-ui`. (`:ui-scaffolds` already depends on
  `:monetization-api` for `ObserveProStatusUseCase`; that api is SDK-free and the edge stays as is.)
- Additive and source-compatible where possible; the one unavoidable break is called out
  (open question Q1).
- Row set, copy, icons and URLs are configuration; the toolkit hardcodes no app.

## What Faint has today (read from `faint/mobile/shared`)

| Faint piece | What it does |
|---|---|
| `FaintSettingsCatalog` / `faintSettingsCatalog(isPro, isRestoringPurchases, isHapticsEnabled)` | Pure builder: a **Pro hero** + sections. Free: hero = upgrade offer (chevron plate, tappable → paywall); Subscription = **Restore** (detail line, chevron; while restoring: "Restoring…" label, **progress** control, disabled). Pro: hero = Pro status (`PRO` nameplate, inert); Subscription = **Manage** (detail line). App = **Haptics** (toggle, detail) · **Show Onboarding** · **Rate** · **Send Feedback**. Support = **Website** · **Privacy** · **Terms**. |
| `FaintSettingsRowSpec` / `FaintSettingsRowControl` | Row = label + optional detail + a trailing control: `Value`, `Count`, `Toggle`, `Chevron`, `ChevronBadge`, `Badge`, `Progress`; flags `disabled`, `destructive`, `showsChevron`. |
| `PoSettingsSection` / `PoSettingsRow` (`ui/settings/SettingsSection.kt`) | Header + card of rows; row = optional leading glyph, label (ink / destructive / muted-when-disabled), detail, trailing control (`PoToggle`, `PoValueChip`, `PoSettingsChevron`, `PoSettingsProgress`, `PoCountBadge`). |
| `PoProHero` | Full-width inverted "ink" plate on the button shadow: accent eyebrow, bold title, muted detail, trailing badge (chevron plate or `PRO` nameplate); `onClick = null` when entitled. No header, no card. |
| `FaintSettingsFooter` | Decorative glyph + tagline + 🇨🇷 on one line, app version below. |
| `FaintSettingsViewModel` | Observes `EntitlementManager.isPro` and the haptics preference; **restore** = `SyncAuthUseCase.identify()` gate → `restorePurchases()` → restored message / `ShowNoSubscriptionFound` / already-owned / error message, with `isRestoringPurchases` for the progress row; **manage** = `manageSubscriptionsUrl()` with double-tap drop; **feedback** = `FeedbackEmail.draft(...)` → `ComposeFeedbackEmail(mailto, recipient)` (screen falls back to a "no mail app, write to …" dialog); **rate / website / privacy / terms** = `OpenUrl`; **onboarding / paywall** = navigation effects; haptics toggle writes the preference. |

## What frnk has today

`:ui-scaffolds` `ui/scaffolds/settings/`: `FrnkSettingsScreen(state, onIntent)` over
`SettingsScreenState(topBar, sections, footer, developerSection, …)`; rows
`SettingsThemeRowState` / `SettingsClickableRowState` (icon, title, subtitle, `SettingsAction`, always
a chevron) / `SettingsToggleRowState` / `SettingsStatusRowState` (badge); `SettingsFooterState(text,
version)`; `SettingsViewModel` (reduces theme/toggle/version-tap, emits `AppearanceChanged` /
`ToggleChanged` / `ActionInvoked`, adopts a recomputed catalogue through `ConfigChanged` while keeping
in-session interaction state); `defaultSettingsState(version, appearance, isPro, …, extraSections)`.
`:shared-monetization-ui`: `rememberFrnkSettingsHandler(backStack, entitlements, analytics, onMessage,
fallback)` maps `UpgradeToPro` / `RestorePurchases` / `ManageSubscription` / god-mode / haptics.

**Gaps against Faint:** no Pro hero; rows can't show a detail-only trailing value, count, progress,
disabled or destructive state; icon is mandatory; no restore-in-progress; restore reports a one-line
message instead of the No Subscription Found dialog; no website row; no footer glyph; feedback /
rate / legal URLs are host code; the haptics toggle's initial value is passed in by hand.

## Proposal

### 1. Extend the state vocabulary (`:ui-scaffolds`, SDK-free, additive)

```kotlin
// SettingsScreenState gains one optional slot above the sections (default null = today's screen).
data class SettingsScreenState(
    …,
    val hero: SettingsHeroState? = null
)

/** Faint's Pro plate, generalised: a standalone plate above the sections. */
@Immutable
data class SettingsHeroState(
    val eyebrow: FrnkStringSource,
    val title: FrnkStringSource,
    val detail: FrnkStringSource? = null,
    val trailing: SettingsHeroTrailing,          // Chevron | Badge(label, contentDescription)
    val action: SettingsAction? = null           // null = inert status plate
)

// SettingsClickableRowState: new defaulted fields, so every existing call site still compiles.
data class SettingsClickableRowState(
    override val id: String,
    val icon: FrnkIconSource?,                   // becomes nullable (Faint rows carry no glyph)
    val title: FrnkStringSource,
    val subtitle: FrnkStringSource? = null,
    val action: SettingsAction,
    val trailing: SettingsRowTrailing = SettingsRowTrailing.Chevron,  // Chevron | Value(text) | Count(n) | Progress | None
    val enabled: Boolean = true,
    val destructive: Boolean = false
)

// SettingsToggleRowState.icon becomes nullable too; SettingsFooterState gains `glyph: FrnkIconSource? = null`.
```

Making `icon` nullable is source-compatible for constructors (non-null arguments still type-check)
but not for hosts that *read* `row.icon` as non-null — acceptable in `0.x` (see Q1).

**Renderer.** `FrnkSettingsScreen` renders the hero (new `FrnkHeroPlate` organism in `:ui-components`,
generic, no subscription knowledge: inverted `colorOnSurface` fill, `colorSurface` text, `colorPrimary`
eyebrow, trailing slot) and the new trailing variants. `SettingsRowTrailing.Progress` needs a
toolkit progress atom: add **`FrnkProgressIndicator`** (indeterminate, token-styled, with
`ProgressBarRangeInfo.Indeterminate` semantics) to `:ui-components`.

### 2. One catalogue builder, configured by the host (`:ui-scaffolds`)

`defaultSettingsState` stays as is. Faint's layout becomes a second, richer builder over the same
types — the "configuration" the plan asks for:

```kotlin
@Immutable
data class FrnkSettingsConfig(
    val version: String,
    val links: FrnkSettingsLinks,                // website?, privacyPolicy, termsOfService, rateApp?
    val showAppearance: Boolean = true,          // Faint/Still: false (single appearance)
    val showHaptics: Boolean = true,
    val showOnboarding: Boolean = true,
    val showRateApp: Boolean = true,
    val footerGlyph: FrnkIconSource? = null,
    val icons: FrnkSettingsIcons = FrnkSettingsIcons.Default,   // per-row glyph or null (Faint: none; Still: Lucide set)
    val extraSections: List<SettingsSectionState> = emptyList(),
    val extraSectionsPlacement: SettingsExtraSectionsPlacement = SettingsExtraSectionsPlacement.BeforeLegal
)

/** The status the catalogue depends on; supplied by the ViewModel, never by the host. */
data class FrnkSettingsStatus(val isPro: Boolean, val isRestoring: Boolean, val hapticsEnabled: Boolean)

fun frnkSettingsCatalog(config: FrnkSettingsConfig, status: FrnkSettingsStatus): SettingsScreenState
```

Order and rows follow Faint exactly: hero → Subscription (Restore with detail + progress while
restoring, or Manage) → App (Haptics toggle, Show Onboarding, Rate, Send Feedback) → Support (Website,
Privacy, Terms) → footer. Copy comes from theme tokens (new ones listed in §5), so Still's "Still Pro"
wording is a `stringOverrides` entry, not code.

### 3. One ViewModel, with an SDK-free subscription seam

Keep **`SettingsViewModel`** as the only settings VM. Add an optional delegate interface in
`:ui-scaffolds` (pure Kotlin, no SDK):

```kotlin
interface SettingsSubscriptionDelegate {
    val isPro: StateFlow<Boolean>
    suspend fun restore(): SettingsRestoreOutcome          // Restored | NothingFound | AlreadyOwned | Failed(FrnkStringSource)
    suspend fun manageSubscriptionsUrl(): String
}
```

- `SettingsViewModel` gets optional constructor parameters (`delegate: SettingsSubscriptionDelegate?`,
  `hapticsPreference: HapticsPreference?`, `config: FrnkSettingsConfig?`) resolved with `getOrNull()`
  in `settingsScaffoldModule`, so existing hosts that bind none of them see today's behaviour.
- With a config + delegate bound, the VM **owns the catalogue**: it observes `isPro` and the haptics
  preference, holds `isRestoring`, and rebuilds the rows through `frnkSettingsCatalog` (the
  `ConfigChanged` merge keeps in-session toggle state). This finishes the groundwork the VM's KDoc
  already describes ("groundwork for the Settings VM to own the subscription catalogue").
- Intents stay `RowClicked(action)` / `ToggleChanged` / `ThemeSelected`; the VM handles
  `RestorePurchases` (restore-in-flight guard, progress row), `ManageSubscription` (double-tap drop),
  `SendFeedback` (`FeedbackEmail.draft`), and the link rows itself, and the haptics toggle writes the
  `HapticsPreference` (A5).
- New effects: `OpenUrl(url)`, `ComposeFeedbackEmail(mailtoUri, recipient)`,
  `ShowNoSubscriptionFound`, `Message(FrnkStringSource)`, `NavigateToPaywall`, `NavigateToOnboarding`.

### 4. The entitlement-aware half (`:shared-monetization-ui`)

- **`EntitlementSettingsDelegate(entitlements: EntitlementManager, syncAuth: SyncAuthUseCase)`**
  implements the seam exactly as Faint's VM does: `identify()` gate (failure →
  `Failed(stringPaywallIdentityError)`), `restorePurchases()` → `Restored` / `NothingFound`, error
  mapping through the existing `MonetizationError.toStringSource()`, `manageSubscriptionsUrl()` with
  the platform fallback. Bound by `frnkSettingsModule(config)` (Koin), which also binds the config.
- **`FrnkSettingsDestination(onBack, onNavigateToPaywall, onShowOnboarding, onMessage, modifier)`** —
  the drop-in screen: `FrnkScreen(koinViewModel<SettingsViewModel>())` + `FrnkSettingsScreen`, and it
  consumes the new effects: `LocalUriHandler` for `OpenUrl`, the feedback launcher for
  `ComposeFeedbackEmail` (falling back to a `FrnkDialog` that shows the recipient when no mail app
  opens), **`NoSubscriptionFoundDialog`** (A3) for `ShowNoSubscriptionFound`, `onMessage` for the rest.
  Mirrors `FrnkPaywallDestination`.
- `rememberFrnkSettingsHandler` stays for hosts on `defaultSettingsState`; it is not the path for the
  Faint layout. Deprecate it once `FrnkSettingsDestination` has a second consumer.

### 5. New theme tokens (`:ui-theme`, EN + ES)

`stringSectionApp`, `stringWebsite`, `stringProHeroEyebrow`, `stringUpgradeDetail`,
`stringProStatusDetail`, `stringRestoreDetail`, `stringRestoreInProgress`,
`stringManageSubscriptionDetail`, `stringLinkUnavailable`, `stringNoMailApp` (`{recipient}`
placeholder, same convention as A3's `{product}`); icons `iconWebsite`, `iconSettingsFooter`
(default: none).

### Module split

| Module | Adds |
|---|---|
| `:ui-theme` | tokens (§5) |
| `:ui-components` | `FrnkHeroPlate` organism, `FrnkProgressIndicator` atom |
| `:ui-scaffolds` | `SettingsHeroState`, `SettingsRowTrailing`, row/footer fields, `FrnkSettingsConfig` / `FrnkSettingsLinks` / `FrnkSettingsIcons` / `FrnkSettingsStatus`, `frnkSettingsCatalog`, `SettingsSubscriptionDelegate` + `SettingsRestoreOutcome`, VM changes, new effects |
| `:shared-monetization-ui` | `EntitlementSettingsDelegate`, `frnkSettingsModule(config)`, `FrnkSettingsDestination` |
| `:haptics` | nothing new (A5's `HapticsPreference` is read by the VM) |

### Faint → frnk mapping

| Faint | frnk |
|---|---|
| `FaintSettingsCatalog` / `faintSettingsCatalog(…)` | `frnkSettingsCatalog(config, status)` |
| `FaintSettingsHeroSpec` + `PoProHero` | `SettingsHeroState` + `FrnkHeroPlate` |
| `FaintSettingsRowSpec` + `FaintSettingsRowControl` | `SettingsClickableRowState.trailing` / `SettingsToggleRowState` / `SettingsStatusRowState` |
| `PoSettingsSection` / `PoSettingsRow` | the existing `FrnkSectionCard`-based renderer, extended |
| `PoSettingsProgress` | `FrnkProgressIndicator` |
| `FaintSettingsFooter` | `SettingsFooterState(text, version, glyph)` |
| `FaintSettingsViewModel` | `SettingsViewModel` + `EntitlementSettingsDelegate` |
| `HapticsPreferenceDataSource` | `HapticsPreference` (A5, done) |
| `NoSubscriptionFoundDialog` / `FaintAppStore` | `NoSubscriptionFoundDialog` / `FrnkAppStore` (A3, done) |
| `FaintSettingsScreen` effect wiring | `FrnkSettingsDestination` |

Still then ships `frnkSettingsModule(FrnkSettingsConfig(version = …, links = …, showAppearance = false,
icons = stillIcons, footerGlyph = …))` plus `hapticsPreferenceModule("still.haptics.enabled")`, and
mounts `FrnkSettingsDestination` at its pushed Settings route.

## Testing plan

- `commonTest` (`:ui-scaffolds`): `frnkSettingsCatalog` Free / Pro / restoring / haptics off / flags
  off; `SettingsViewModel` with a fake delegate: restore outcomes → effects, restore-in-flight guard,
  manage double-tap drop, haptics toggle writes the preference, `ConfigChanged` keeps toggles.
- `commonTest` (`:shared-monetization-ui`): `EntitlementSettingsDelegate` against the existing
  `FakeEntitlementManager`-style fakes (identity failure, nothing found, already owned).
- `androidHostTest`: `FrnkSettingsScreen` renders the hero and the trailing variants;
  `FrnkSettingsDestination` shows `NoSubscriptionFoundDialog` on an empty restore; port Faint's
  `FaintSettingsViewModelTest` cases.
- Previews for `FrnkHeroPlate`, `FrnkProgressIndicator` and the Faint-layout catalogue (light/dark).

## Open questions

1. **Compatibility budget.** New `SettingsEffect` / `SettingsAction` subtypes break hosts that `when`
   over them exhaustively, and a nullable `icon` breaks hosts that read it. Ship A4 as **0.11.0**
   (allowed by the pre-1.0 policy, with a "Changed" section), or nest the new effects under one
   `SettingsEffect.Subscription` subtype to limit the break?
2. **Where dialogs live.** Faint hosts every dialog at the shell level. Should `FrnkStackShell` grow a
   dialog presentation (`FrnkShellNavigator.alert(…)`) so `FrnkSettingsDestination` raises
   No Subscription Found through it, or is drawing it over the Settings destination enough for v1?
3. **Hero look.** Is the inverted `colorOnSurface` plate the right token mapping for Faint's ink slab,
   and does it need a shadow? frnk ships no default shadow token (`Theme[shadows]` is empty by default).
4. **Website row.** A new `SettingsAction.Website`, or a generic `SettingsAction.OpenLink(id)` whose
   URL comes from `FrnkSettingsLinks`? The generic one also covers future link rows.
5. **Rate-the-app URL.** Faint builds it per platform (`faintRateAppUrl()`). Should frnk provide
   `platformRateAppUrl(appStoreId, packageName)` in `:shared-monetization-ui` (App Store needs an id
   Android does not)?
6. **Restore analytics.** Faint records `restore_*` events (`RestoreAnalytics`). Toolkit
   `ToolkitEvent`s in the delegate, or left to the host via callbacks (as A3's `onShown` does)?
7. **Paywall parity.** Faint's paywall footer also raises No Subscription Found on an empty restore;
   frnk's `PaywallViewModel` shows `stringPaywallNothingToRestore`. Change it in A4 or separately?
8. **Mail fallback.** `rememberFeedbackEmailLauncher` must report "no mail app opened" for the
   recipient fallback dialog. Confirm the launcher can detect that on both platforms (Faint's link
   opener reports it through a callback).
9. **Row taps on toggles.** Faint lets a tap anywhere on the haptics row throw the switch. Adopt that
   for every `SettingsToggleRowState`?