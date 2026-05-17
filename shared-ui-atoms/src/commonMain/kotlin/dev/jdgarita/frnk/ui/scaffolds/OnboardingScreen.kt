package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconClose
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.stringBack
import dev.jdgarita.frnk.ui.theme.stringClose
import dev.jdgarita.frnk.ui.theme.stringGetStarted
import dev.jdgarita.frnk.ui.theme.stringNext
import dev.jdgarita.frnk.ui.theme.strings
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * VM-backed convenience wrapper around [OnboardingScreenContent]. Resolves an
 * [OnboardingViewModel] from Koin (initialised with [initialState] via `parametersOf`), forwards
 * its state to the stateless renderer, and surfaces one-shot effects to [onEffect].
 *
 * The [vmKey] parameter scopes the ViewModel inside the host's `ViewModelStore`. By default the
 * VM is reused for the lifetime of the enclosing `ViewModelStoreOwner` (typically the Activity),
 * so dismissing and re-opening the overlay reuses the same VM — the user lands back on whichever
 * page they left off. Hosts that want a fresh flow on every open should change [vmKey] each time
 * the overlay is shown (e.g. `key = "onboarding-$openCounter"` where `openCounter++` on each show).
 *
 * Hosts that want full control over state hoisting (or that already use a different DI/MVI stack)
 * should call [OnboardingScreenContent] directly.
 */
@Composable
fun OnboardingScreen(
    initialState: OnboardingScreenState,
    modifier: Modifier = Modifier,
    vmKey: String? = null,
    onEffect: (OnboardingEffect) -> Unit = {},
) {
    val vm: OnboardingViewModel = koinViewModel(key = vmKey) { parametersOf(initialState) }
    val state by vm.state.collectAsState()

    // The collection coroutine is keyed on `vm`, so it only restarts when the VM identity changes.
    // Wrap [onEffect] in rememberUpdatedState so a recomposition with a new lambda (e.g. one that
    // captures fresh outer-scope state) is observed by the long-lived collector — otherwise the
    // first-composition lambda would be captured forever.
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(vm) { vm.effects.collect { currentOnEffect(it) } }

    OnboardingScreenContent(
        state = state,
        onIntent = vm::send,
        modifier = modifier,
    )
}

/**
 * Stateless renderer. The pager position is synced two ways with [OnboardingScreenState.currentPageIndex]:
 *  - user swipe → `PagerState.currentPage` changes → emit [OnboardingIntent.PageSelected]
 *  - host updates state.currentPageIndex (e.g. via Next/Back) → animate the pager to that page
 *
 * Two separate [LaunchedEffect]s keyed by the source-of-truth on each side prevent a feedback loop.
 *
 * **Layout note:** when [OnboardingScreenState.pagerHeight] is `null`, the pager uses
 * `Modifier.weight(1f)` inside this function's own `Column`, so it fills the **Column's** remaining
 * vertical space (the area left over after the top-right close button, pips, and button row). This
 * function provides that `Column`; advanced callers don't need to wrap it in one, and they don't
 * need to worry about `weight` being a no-op in a non-Column parent.
 */
@Composable
fun OnboardingScreenContent(
    state: OnboardingScreenState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageCount = state.pages.size
    val pagerState = rememberPagerState(initialPage = state.currentPageIndex) { pageCount }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != state.currentPageIndex) onIntent(OnboardingIntent.PageSelected(page))
        }
    }
    LaunchedEffect(state.currentPageIndex) {
        if (pagerState.currentPage != state.currentPageIndex) {
            pagerState.animateScrollToPage(state.currentPageIndex)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Theme[colors][colorBackground])
                .padding(horizontal = FrnkSpacing.lg, vertical = FrnkSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            FrnkIconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                state =
                    FrnkIconButtonState(
                        imageVector = Theme[icons][iconClose],
                        contentDescription = Theme[strings][stringClose],
                        tint = colorOnSurfaceVariant,
                    ),
                onClick = { onIntent(OnboardingIntent.CloseClicked) },
            )
        }

        val pagerModifier =
            if (state.pagerHeight != null) {
                Modifier.fillMaxWidth().height(state.pagerHeight)
            } else {
                Modifier.fillMaxWidth().weight(1f)
            }

        HorizontalPager(
            state = pagerState,
            modifier = pagerModifier,
            userScrollEnabled = state.userScrollEnabled,
        ) { page ->
            OnboardingPageContent(state = state.pages[page])
        }

        OnboardingPips(
            pageCount = pageCount,
            currentPage = state.currentPageIndex,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        OnboardingNavigationRow(
            isFirstPage = state.isFirstPage,
            isLastPage = state.isLastPage,
            onPrevious = { onIntent(OnboardingIntent.PreviousClicked) },
            onNext = { onIntent(OnboardingIntent.NextClicked) },
        )
    }
}

@Composable
private fun OnboardingPageContent(state: OnboardingPageState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = FrnkSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.icon?.let { FrnkIcon(state = it) }
        FrnkText(state = state.title.centered(), modifier = Modifier.fillMaxWidth())
        state.description?.let { FrnkText(state = it.centered(), modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun OnboardingNavigationRow(
    isFirstPage: Boolean,
    isLastPage: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isFirstPage) {
            Spacer(modifier = Modifier.weight(1f))
        } else {
            FrnkButton(
                state =
                    FrnkButtonState(
                        text = Theme[strings][stringBack],
                        variant = FrnkButtonVariant.Outlined,
                    ),
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
            )
        }

        val nextLabel = Theme[strings][if (isLastPage) stringGetStarted else stringNext]
        FrnkButton(
            state = FrnkButtonState(text = nextLabel),
            onClick = onNext,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OnboardingPips(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val activeColor = Theme[colors][colorPrimary]
    val inactiveColor = Theme[colors][colorOutline]
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                label = "pip_color",
            )
            val pipSize by animateDpAsState(
                targetValue = if (isActive) 10.dp else 8.dp,
                label = "pip_size",
            )
            Box(
                modifier =
                    Modifier
                        .size(pipSize)
                        .clip(Theme[shapes][shapeFull])
                        .background(color),
            )
        }
    }
}

/** Force a centred alignment on the variants that ship a non-centre default. */
private fun FrnkTextState.centered(): FrnkTextState =
    when (this) {
        is FrnkTextState.Title -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.TitleMedium -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.HeadlineSmall -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.Body -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.BodyMedium -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.BodySmall -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.AppName -> copy(textAlign = TextAlign.Center)
        is FrnkTextState.Raw -> copy(textAlign = TextAlign.Center)
    }
