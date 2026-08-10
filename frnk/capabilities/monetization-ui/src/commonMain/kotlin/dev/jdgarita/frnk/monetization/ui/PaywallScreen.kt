package dev.jdgarita.frnk.monetization.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.scaffolds.FrnkFullScreenScaffold
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconSizeLg
import dev.jdgarita.frnk.ui.theme.iconSizeSm
import dev.jdgarita.frnk.ui.theme.iconSizes
import dev.jdgarita.frnk.ui.theme.iconUpgrade
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXxs
import dev.jdgarita.frnk.ui.theme.stringAppName
import dev.jdgarita.frnk.ui.theme.stringPaywallContinue
import dev.jdgarita.frnk.ui.theme.stringPaywallEmpty
import dev.jdgarita.frnk.ui.theme.stringPaywallFreeTrialBadge
import dev.jdgarita.frnk.ui.theme.stringPaywallPrivacy
import dev.jdgarita.frnk.ui.theme.stringPaywallRestoring
import dev.jdgarita.frnk.ui.theme.stringPaywallStartTrial
import dev.jdgarita.frnk.ui.theme.stringPaywallTerms
import dev.jdgarita.frnk.ui.theme.stringPaywallTitlePrefix
import dev.jdgarita.frnk.ui.theme.stringPerMonthSuffix
import dev.jdgarita.frnk.ui.theme.stringProName
import dev.jdgarita.frnk.ui.theme.stringRestorePurchases
import dev.jdgarita.frnk.ui.theme.strings
import org.koin.compose.viewmodel.koinViewModel

/**
 * The toolkit's basic paywall — a full screen of stacked, selectable plan cards over a frnk-owned
 * [PaywallViewModel]. Hosts can ship their own paywall instead; this is mounted via
 * [frnkPaywallNavigation] so the toolkit owns the route.
 *
 * @param source analytics source (where the paywall was opened from).
 * @param features short benefit bullets shown above the plans (host-supplied).
 */
@Composable
fun PaywallScreen(
    source: String,
    features: List<String>,
    modifier: Modifier = Modifier,
    vmKey: String? = null,
    onEffect: (PaywallEffect) -> Unit = {}
) {
    val vm: PaywallViewModel = koinViewModel(key = vmKey)
    FrnkScreen(
        viewModel = vm,
        arguments = PaywallArguments(source),
        onEffect = { effect ->
            when (effect) {
                is PaywallEffect -> onEffect(effect)
                // FrnkScreen installs a BackHandler that routes system back to CommonUiEffect.DidPressBack;
                // treat it as a close so the paywall dismisses on back, mirroring the ✕.
                is CommonUiEffect.DidPressBack -> vm.send(PaywallIntent.Close)
                else -> Unit
            }
        }
    ) { state ->
        FrnkFullScreenScaffold(
            onCloseClick = { vm.send(PaywallIntent.Close) },
            modifier = modifier,
            contentPadding = PaddingValues(Theme[spacing][spacingLg])
        ) { padding ->
            // The scaffold folds safe-area insets + the close-button band into `padding`; applying it as
            // the scroll's contentPadding makes the header clear the ✕ and the list scroll under it.
            PaywallScreenContent(
                state = state,
                features = features,
                onIntent = vm::send,
                contentPadding = padding
            )
        }
    }
}

/** Stateless paywall body — header, feature checklist, plan cards, CTA, restore + legal. */
@Composable
fun PaywallScreenContent(
    state: PaywallScreenState,
    features: List<String>,
    onIntent: (PaywallIntent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg])
) {
    val title = "${Theme[strings][stringPaywallTitlePrefix]} ${Theme[strings][stringAppName]} ${Theme[strings][stringProName]}"
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingLg])
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm]),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FrnkIcon(
                state =
                    FrnkIconState.Content(
                        imageVector = Theme[icons][iconUpgrade],
                        contentDescription = null,
                        size = Theme[iconSizes][iconSizeLg],
                        tint = colorPrimary
                    )
            )
            FrnkText(state = FrnkTextState.HeadlineSmall(text = title))
        }

        if (features.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])) {
                features.forEach { FeatureRow(text = it) }
            }
        }

        when {
            state.isLoading -> repeat(2) { SkeletonCard() }
            state.products.isEmpty() ->
                FrnkText(state = FrnkTextState.Body(text = Theme[strings][stringPaywallEmpty], color = colorOnSurfaceVariant))
            else ->
                Column(verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])) {
                    state.products.forEach { product ->
                        PaywallProductCard(
                            product = product,
                            selected = product.id == state.selectedProductId,
                            onClick = { onIntent(PaywallIntent.ProductSelected(product.id)) }
                        )
                    }
                }
        }

        val ctaText =
            if (state.selectedProduct?.hasFreeTrial == true) {
                Theme[strings][stringPaywallStartTrial]
            } else {
                Theme[strings][stringPaywallContinue]
            }
        FrnkButton(
            state =
                FrnkButtonState.Content(
                    text = ctaText,
                    enabled = state.selectedProductId != null && !state.isPurchasing
                ),
            onClick = { onIntent(PaywallIntent.Purchase) },
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs])
        ) {
            FrnkButton(
                state =
                    FrnkButtonState.Content(
                        text =
                            if (state.isRestoring) {
                                Theme[strings][stringPaywallRestoring]
                            } else {
                                Theme[strings][stringRestorePurchases]
                            },
                        variant = FrnkButtonVariant.Ghost,
                        enabled = !state.isRestoring
                    ),
                onClick = { onIntent(PaywallIntent.Restore) }
            )
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        text = "${Theme[strings][stringPaywallTerms]} · ${Theme[strings][stringPaywallPrivacy]}",
                        style = labelSmall,
                        color = colorOnSurfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm]),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FrnkIcon(
            state =
                FrnkIconState.Content(
                    imageVector = Theme[icons][iconCheck],
                    contentDescription = null,
                    size = Theme[iconSizes][iconSizeSm],
                    tint = colorPrimary
                )
        )
        FrnkText(state = FrnkTextState.Body(text = text))
    }
}

@Composable
private fun PaywallProductCard(
    product: ProProduct,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(Theme[shapes][shapeCard])
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Theme[colors][colorPrimary] else Theme[colors][colorOutline],
                    shape = Theme[shapes][shapeCard]
                ).background(Theme[colors][colorSurface])
                .clickable { onClick() }
                .padding(Theme[spacing][spacingMd]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioDot(selected = selected)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs])) {
            FrnkText(state = FrnkTextState.TitleMedium(text = product.title))
            product.pricePerMonthFormatted?.let { perMonth ->
                FrnkText(
                    state =
                        FrnkTextState.BodySmall(
                            text = "$perMonth${Theme[strings][stringPerMonthSuffix]}",
                            color = colorOnSurfaceVariant
                        )
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs])) {
            FrnkText(state = FrnkTextState.TitleMedium(text = product.priceFormatted))
            val badge = product.badge ?: if (product.hasFreeTrial) Theme[strings][stringPaywallFreeTrialBadge] else null
            badge?.let {
                FrnkText(state = FrnkTextState.Raw(text = it, style = labelSmall, color = colorPrimary))
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
        modifier =
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) Theme[colors][colorPrimary] else Theme[colors][colorOutline],
                    shape = CircleShape
                ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Theme[colors][colorPrimary]))
        }
    }
}

@Composable
private fun SkeletonCard() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(Theme[shapes][shapeCard])
                .border(width = 1.dp, color = Theme[colors][colorOutline], shape = Theme[shapes][shapeCard])
                .padding(Theme[spacing][spacingMd])
    ) {
        FrnkText(
            state =
                FrnkTextState.TitleMedium(
                    text = "Plan placeholder",
                    skeleton = FrnkSkeleton(enabled = true)
                )
        )
    }
}