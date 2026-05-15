package dev.jdgarita.frnk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.ui.atoms.ProvideToolkitTheme
import dev.jdgarita.frnk.ui.atoms.ToolkitButton
import dev.jdgarita.frnk.ui.atoms.ToolkitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideToolkitTheme(colors = demoBlueColors()) {
                DemoRoot(::handleEffect)
            }
        }
    }

    private fun handleEffect(effect: DemoEffect) =
        when (effect) {
            is DemoEffect.Navigate -> Toast.makeText(this, "Navigate → ${effect.routeKey}", Toast.LENGTH_SHORT).show()
            is DemoEffect.Toast -> Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
        }
}

@Composable
private fun DemoRoot(onEffect: (DemoEffect) -> Unit) {
    var backend by remember { mutableStateOf(BackendChoice.Supabase) }
    // Bumped on backend swap so the inner DemoScreen is re-keyed and koinViewModel() re-resolves
    // against the freshly-restarted Koin container.
    var nonce by remember { mutableIntStateOf(0) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(ToolkitTheme.colors.surface),
    ) {
        BackendSwitcher(
            current = backend,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            onSelect = { choice ->
                if (choice != backend) {
                    swapBackend(choice)
                    backend = choice
                    nonce += 1
                }
            },
        )
        key(nonce) {
            DemoScreen(onEffect = onEffect)
        }
    }
}

@Composable
private fun BackendSwitcher(
    current: BackendChoice,
    onSelect: (BackendChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(
            "Backend:",
            style = ToolkitTheme.typography.button.copy(color = ToolkitTheme.colors.onSurface),
        )
        BackendChoice.entries.forEach { choice ->
            ToolkitButton(
                label = if (choice == current) "[${choice.name}]" else choice.name,
                onClick = { onSelect(choice) },
            )
        }
    }
}
