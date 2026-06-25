# ui-components

The toolkit's Compose Multiplatform **component tier**: atoms, molecules, organisms, and the vendored
placeholder/skeleton machinery, built on `compose-unstyled` 2.x. Headless and unopinionated — **not**
Material3. Extracted from `:shared-ui-atoms` at restructure **Stage 7b**; sits above `:ui-theme`
(tokens + theme engine) / below `:ui-scaffolds`
(`haptics ← ui-theme ← ui-components ← ui-scaffolds ← ui-bottom-nav ← ui-app`). Kotlin packages
unchanged (`dev.jdgarita.frnk.ui.{atoms,molecules,organisms,placeholder}`).

Tokens, the theme engine (`FrnkTheme`/`FrnkThemeConfig`/`FrnkStrings`/`FrnkIcons`/`FrnkRipple`), and the
haptics binding now live in `:ui-theme` / `:haptics`; the scaffolds + Compose MVI/Nav bindings live in
`:ui-scaffolds`. Atoms read those via `:ui-theme`'s `api` export (`Theme[colors][...]`,
`LocalFrnkHaptics`, …).

## Contents

- `ui/atoms/` — `Frnk*` atoms, each in its own file with an `@Immutable` `*State` class. **Interactive atoms auto-fire haptics on interaction** via `LocalFrnkHaptics.current.perform(...)` (gated by the global enabled flag): `FrnkButton`/`FrnkIconButton` → `HapticType.Click`; `FrnkSwitch`/`FrnkSegmentedControl` → `HapticType.Selection` (multi-option selectors fire only on actual change). New interactive atoms should follow suit.
  - `FrnkSkeleton.kt` — `FrnkSkeleton` (`@Immutable`) + `FrnkSkeletonHighlight` (Shimmer/Fade/None) + the internal `Modifier.frnkSkeleton(...)`. **State architecture — three sanctioned `*State` shapes** (full taxonomy in `docs/HOST_INTEGRATION.md` §9): **(A) Stateful** — a `sealed interface` + `Content` data class + `data object Skeleton`, the default for loading-capable atoms (the `when` Skeleton branch is a non-interactive token-styled block via `Modifier.frnkSkeleton(...)`); **(B) Variant** — a `sealed class` whose variants share stored `open val` fields (`FrnkDividerState`; `FrnkTextState`, which also keeps a per-subtype `skeleton` field **and** a `Skeleton` object); **(C) Single-state** — a plain `@Immutable data class`, no `Skeleton`, for terminal/chrome/delegated states. Categories B and C each carry a one-line `State shape — Category X` marker at the declaration. All skeleton values resolve from theme tokens. Backed by the vendored `ui/placeholder/`.
  - `FrnkText.kt` — sealed `FrnkTextState`. The text variants (Raw/Title/TitleMedium/HeadlineSmall/Body/BodyMedium/BodySmall) are `Resolvable` subtypes holding a single `content: FrnkStringSource` resolved at the leaf; each also exposes a `String` secondary constructor (wraps `FrnkStringSource.Raw`) so `FrnkTextState.Title(text = "…")` call sites are unchanged. `AppName` carries an `AnnotatedString` (the one non-`Resolvable`). `FrnkTextDefaultSkeleton` (public top-level val) is the shared default-skeleton instance reused by every subtype.
  - `FrnkButton.kt` — `FrnkButtonState` + `FrnkButtonVariant` (Filled/Outlined/Ghost).
  - `FrnkIcon.kt` / `FrnkIconButton.kt` — `FrnkIconState` / `FrnkIconButtonState`. `FrnkIconState.Content.icon: FrnkIconSource` (theme `Token` or host `Vector`), resolved at the leaf; an `ImageVector` secondary constructor keeps raw call sites unchanged.
  - `FrnkDivider.kt` — sealed `FrnkDividerState` (Horizontal/Vertical).
  - `FrnkSwitch.kt` — `FrnkSwitchState`; animated on/off toggle on foundation primitives (`toggleable`, `Role.Switch`), no Material3.
  - `FrnkSegmentedControl.kt` — `FrnkSegmentedControlState` (options + selectedIndex); n-way single-select pill (drives the settings theme toggle).
  - `FrnkTopAppBar.kt` — `FrnkTopAppBarState` (title + optional `navigationIcon` + `actions`); status-bar-safe, with a search mode. `FrnkTopAppBarDefaults.BarHeight` (56.dp) for hosts floating the bar over edge-to-edge content.
  - `BottomNavInsets.kt` — `frnkBottomSystemBarInset()`, the bottom system-nav inset read shared with `:ui-bottom-nav`'s adaptive bar `reservedHeight`.
- `ui/molecules/` — **Molecules tier** (P4-1): small compositions of atoms + tokens. Each has an `@Immutable *State`, callbacks before `modifier`, tokens-only styling, a `@Preview` in `commonDebug/.../ui/molecules/previews/`, a recorded skeleton decision; interactive molecules reuse the automatic ripple + haptics.
  - `FrnkListRow.kt` — `FrnkListRowState` + `FrnkListRow(state, onClick?, modifier, trailing?, swipe?, …)`. Icon → title/subtitle column → trailing slot; optional whole-row `onClick`; optional `FrnkSwipeable` wrap. Skeleton: **yes**.
  - `FrnkSwipeable.kt` (+ `FrnkSwipeAction.kt`, `FrnkSwipeController.kt`) — the generic swipe-to-action primitive (Dismiss/Reveal) usable around any content. Pure Compose Foundation; haptics via `LocalFrnkHaptics`. Skeleton: **no, by design** (interaction chrome). **Headless, Material3-free reimplementation** of stevdza-san/Swipeable-KMP.
  - `FrnkLabeledValue.kt` — label + value (Inline/Stacked). Skeleton: **yes** (value only).
  - `FrnkEmptyState.kt` — centered icon/title/subtitle + optional CTA. Skeleton: **no, by design** (terminal zero-content).
- `ui/organisms/` — **Organisms tier** (P4-2): self-contained UI *sections* from molecules + atoms. Same rules as molecules; pure stateless view code, previews only — no unit tests.
  - `FrnkSectionCard.kt` — `fun <T> FrnkSectionCard(rows, modifier, title?, footnote?, row)`. The **single source of the titled-card chrome** (optional title + `shapeCard`/`colorSurface` card + `FrnkDivider`-between-rows + `animateContentSize()` + optional footnote) with a `row(index, item)` slot. **Public** (was `internal`) since the Stage 7b split — it's shared by the public `FrnkListSection` organism (this module) **and** the Settings scaffold's `SettingsSection` (in `:ui-scaffolds`), so it crosses a module boundary. Hosts normally compose sections via the higher-level entry points, not this directly.
  - `FrnkListSection.kt` — `FrnkListSectionState` + `FrnkListSection(...)`: a thin organism over `FrnkSectionCard` stacking `FrnkListRow`s. Skeleton: **yes, carried by the rows**.
  - `FrnkProfileHeader.kt` — `FrnkProfileHeaderState` + `FrnkProfileHeader(...)`: avatar chip + name/subtitle + optional `FrnkLabeledValue` stat tiles. Skeleton: **yes, passed through**.
- `ui/placeholder/` — the **vendored, Material3-free** skeleton effect (a subset of RevenueCat's Apache-2.0 `placeholder-compose`) backing `FrnkSkeleton`/`Modifier.frnkSkeleton`. See `ui/placeholder/NOTICE.md` for the vendoring rationale + modifications. Don't add the upstream `com.revenuecat.purchases:placeholder` artifact (it hard-depends on Material3).

## Source sets

- `commonMain` — production code (atoms, molecules, organisms, placeholder).
- `androidHostTest` — **Compose UI tests for the highest-value atoms (P4-4).** Headless atoms have no reducer, so behavior is verified by driving a real composition with `runComposeUiTest` + the semantics tree, as JVM **host** tests (`testAndroidHostTest` — what CI gates) under **Robolectric**. Test classes extend `RobolectricComposeTest`. Covered today: `FrnkSwitch`, `FrnkSegmentedControl`, `FrnkTopAppBar`. Molecules/organisms stay previews-only. (`:ui-scaffolds` carries a per-module copy of `RobolectricComposeTest`/`setFrnkContent` — that source set isn't shared; the test *dependency bundle* is, via the `frnk.kmp.library.composehosttest` convention plugin.)
- `commonDebug` — `@Preview` composables for every atom/molecule/organism. Sits between `commonMain` and each platform source set. Wrap content in `PreviewSurface(appearance = ...)`. **Caveat:** AGP 9's single `androidMain` compilation means `commonDebug` also ships in release AARs (inert `@Composable`s R8 strips); promote to a sibling module if true exclusion becomes load-bearing.

## Dependencies

- `api(projects.uiTheme)` — tokens + theme engine; transitively re-exports compose-unstyled `theming` (the `Theme[...]` lookups) and the `:haptics` contract (`LocalFrnkHaptics` / `HapticType`).
- `implementation(compose-unstyled.{primitives,button,icon,separators})` — the headless primitives the atoms wrap; `implementation` keeps the consumer surface slim.
- `implementation(icons-lucide)` — Lucide vectors used by atom previews + the `FrnkTopAppBar` host test. Hosts override every icon via `FrnkThemeConfig`, so consumers don't take a Lucide dependency unless they reference Lucide vectors at their own call sites.
- `api(compose.runtime / foundation / ui)` via `frnk.kmp.library.composehosttest`.

## Rules

- **No Material3.** Atoms wrap `com.composeunstyled.*` headless primitives and resolve styling via `Theme[colors][...]` / `Theme[textStyles][...]`. This is also why the skeleton effect is **vendored** under `ui/placeholder/` rather than taken as a dependency.
- **Atoms read tokens, not hard-coded values.** `Theme[colors][colorPrimary]`, `Theme[spacing][spacingMd]`, `Theme[iconSizes][iconSizeMd]` — never `Color(0xFF...)` / raw `.dp`. The `FrnkSpacing`/`FrnkIconSize` objects (in `:ui-theme`) remain the default-value source for non-composable default-argument expressions only.
- **Atoms are platform-agnostic** — `commonMain` only. No `LocalContext`, `UIViewController`, etc.
- **Every component has an `@Immutable *State` class**; callbacks are separate params before `modifier`.
- **`*State` follows the three-category taxonomy** (`docs/HOST_INTEGRATION.md` §9 — "Component style guide"): **(A)** sealed-interface `Content` + `Skeleton` for loading-capable components (Skeleton branch non-interactive, token-driven); **(B)** shared-field `sealed class` variants — `FrnkDividerState`, `FrnkTextState` (note: `FrnkDividerState` **is** a sealed class, not a plain data class); **(C)** plain `@Immutable data class`, no `Skeleton`, for single-state/terminal/chrome/delegated components (`FrnkTopAppBar`, `FrnkEmptyState`, `FrnkSwipeable`, `FrnkListSection`). Every Category-B/C `*State` carries a one-line `State shape — Category X` marker at its declaration noting why.
- **Press feedback + haptics are automatic** under `FrnkTheme` (`LocalIndication` ripple + `LocalFrnkHaptics`) — don't re-wire; interactive atoms just call `perform(...)`.
- Host configuration flows through `FrnkThemeConfig` (in `:ui-theme`) — the single override entry point.
