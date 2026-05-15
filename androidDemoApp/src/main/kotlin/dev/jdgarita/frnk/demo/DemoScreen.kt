package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.ui.atoms.ToolkitButton
import dev.jdgarita.frnk.ui.atoms.ToolkitTextField
import dev.jdgarita.frnk.ui.atoms.ToolkitTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit. Three sections, top to bottom:
 *  1. Theming + atoms — ToolkitButton / ToolkitTextField read tokens from the active theme.
 *  2. Koin + FeatureGate — the screen demonstrates `gate.canUse` + `gate.requestUpgrade`.
 *  3. MVI engine — state/intent/effect flow through DemoViewModel.
 *
 * The host integration story is: a real app passes its own [dev.jdgarita.frnk.ui.atoms.ToolkitColors],
 * binds a real [dev.jdgarita.frnk.monetization.EntitlementManager] (RevenueCat), and routes
 * [dev.jdgarita.frnk.ui.atoms.ToolkitTheme] through its own NavHost.
 */
@Composable
fun DemoScreen(onEffect: (DemoEffect) -> Unit = {}) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) { vm.effects.collect(onEffect) }

    val colors = ToolkitTheme.colors
    val typography = ToolkitTheme.typography

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.surface)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BasicText("Frnk Toolkit Demo", style = typography.title.copy(color = colors.onSurface))

        Section("1. Theme + Atoms") {
            ToolkitTextField(
                value = state.email,
                onValueChange = { vm.send(DemoIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "you@example.com",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolkitButton(label = "−", onClick = { vm.send(DemoIntent.Decrement) })
                BasicText(
                    "Count: ${state.count}",
                    style = typography.body.copy(color = colors.onSurface),
                )
                ToolkitButton(label = "+", onClick = { vm.send(DemoIntent.Increment) })
            }
        }

        Section("2. FeatureGate (Pro = ${state.isPro})") {
            ToolkitButton(label = "Toggle Pro", onClick = { vm.send(DemoIntent.TogglePro) })
            ToolkitButton(label = "Request Upgrade", onClick = { vm.send(DemoIntent.RequestUpgrade) })
        }

        Section("3. MVI") {
            BasicText(
                "DemoViewModel = MviViewModel<DemoState, DemoIntent, DemoEffect>.\n" +
                    "Every interaction above flows: Composable → send(Intent) → reducer → State → recomposition.",
                style = typography.body.copy(color = colors.onSurface),
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(
            title,
            style = ToolkitTheme.typography.button.copy(color = ToolkitTheme.colors.onSurface),
        )
        content()
        Spacer(Modifier.height(4.dp))
    }
}
