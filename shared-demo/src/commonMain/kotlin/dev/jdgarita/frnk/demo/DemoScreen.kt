package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.background
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.onBackground
import dev.jdgarita.frnk.ui.theme.onSurfaceVariant
import dev.jdgarita.frnk.ui.theme.primary
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit. Sections, top to bottom:
 *  1. Theming + atoms — FrnkText / FrnkButton / FrnkIcon / FrnkIconButton / FrnkDivider read
 *     tokens from the active FrnkTheme.
 *  2. Koin + FeatureGate — exercises gate.canUse + gate.requestUpgrade.
 *  3. MVI engine — state/intent/effect flow through DemoViewModel.
 *
 * The host integration story: a real app passes its own [dev.jdgarita.frnk.ui.theme.FrnkThemeConfig]
 * (color/typography/string/icon overrides), binds a real [dev.jdgarita.frnk.monetization.EntitlementManager]
 * (e.g. RevenueCat), and mounts [DemoScreen] under its own NavHost.
 */
@Composable
fun DemoScreen(onEffect: (DemoEffect) -> Unit = {}) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) { vm.effects.collect(onEffect) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Theme[colors][background])
                .verticalScroll(rememberScrollState())
                .padding(FrnkSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
    ) {
        FrnkText(state = FrnkTextState.HeadlineSmall(text = "Frnk Toolkit Demo"))

        FrnkDivider(state = FrnkDividerState.Horizontal())

        Section(title = "1. Theme + Atoms") {
            FrnkText(state = FrnkTextState.TitleMedium(text = "Text variants"))
            FrnkText(state = FrnkTextState.Body(text = "Body — the default reading style."))
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text = "BodySmall — secondary copy.",
                        color = onSurfaceVariant,
                    ),
            )

            FrnkText(state = FrnkTextState.TitleMedium(text = "Buttons"))
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(
                    state = FrnkButtonState(text = "Filled"),
                    onClick = { vm.send(DemoIntent.Increment) },
                )
                FrnkButton(
                    state = FrnkButtonState(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                    onClick = { vm.send(DemoIntent.Decrement) },
                )
                FrnkButton(
                    state =
                        FrnkButtonState(
                            text = "Ghost",
                            variant = FrnkButtonVariant.Ghost,
                            enabled = false,
                        ),
                    onClick = { },
                )
            }

            FrnkText(state = FrnkTextState.TitleMedium(text = "Icons"))
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FrnkIcon(
                    state =
                        FrnkIconState(
                            imageVector = Theme[icons][iconBack],
                            contentDescription = "Back",
                            size = FrnkIconSize.lg,
                            tint = primary,
                        ),
                )
                FrnkIcon(
                    state =
                        FrnkIconState(
                            imageVector = Theme[icons][iconCheck],
                            contentDescription = "Check",
                            size = FrnkIconSize.lg,
                            tint = primary,
                        ),
                )
                FrnkIconButton(
                    state =
                        FrnkIconButtonState(
                            imageVector = Theme[icons][iconSettings],
                            contentDescription = "Settings",
                            tint = onBackground,
                        ),
                    onClick = { onEffect(DemoEffect.Toast("Settings tapped")) },
                )
            }

            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = "Count: ${state.count} • Email field: pending FrnkTextField (v2)",
                        color = onSurfaceVariant,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FrnkDivider(state = FrnkDividerState.Horizontal())

        Section(title = "2. FeatureGate (Pro = ${state.isPro})") {
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(
                    state =
                        FrnkButtonState(
                            text = "Toggle Pro",
                            variant = FrnkButtonVariant.Outlined,
                        ),
                    onClick = { vm.send(DemoIntent.TogglePro) },
                )
                FrnkButton(
                    state = FrnkButtonState(text = "Request Upgrade"),
                    onClick = { vm.send(DemoIntent.RequestUpgrade) },
                )
            }
        }

        FrnkDivider(state = FrnkDividerState.Horizontal())

        Section(title = "3. MVI") {
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text =
                            "DemoViewModel = MviViewModel<DemoState, DemoIntent, DemoEffect>.\n" +
                                "Every interaction above flows: Composable → send(Intent) → reducer → State → recomposition.",
                        color = onSurfaceVariant,
                    ),
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
        FrnkText(state = FrnkTextState.Title(text = title))
        content()
    }
}
