package dev.jdgarita.frnk.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jdgarita.frnk.ui.atoms.AppButton
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.mvi.ObserveAsEvents
import dev.jdgarita.frnk.ui.mvi.UiAction
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiState

// --- MVI contract ----------------------------------------------------------

private data class CounterState(val count: Int = 0) : UiState

private sealed interface CounterAction : UiAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data object Reset : CounterAction
}

private sealed interface CounterEffect : UiEffect {
    data class ShowToast(val message: String) : CounterEffect
}

// --- ViewModel -------------------------------------------------------------

private class CounterViewModel : MviViewModel<CounterState, CounterAction, CounterEffect>(
    initialState = CounterState(),
) {
    override fun reduce(current: CounterState, action: CounterAction): CounterState = when (action) {
        CounterAction.Increment -> current.copy(count = current.count + 1)
        CounterAction.Decrement -> current.copy(count = (current.count - 1).coerceAtLeast(0))
        CounterAction.Reset -> CounterState()
    }

    override suspend fun onAction(action: CounterAction) {
        val count = state.value.count
        when {
            count == 5 -> emitEffect(CounterEffect.ShowToast("Halfway to 10 🚀"))
            count >= 10 -> emitEffect(CounterEffect.ShowToast("Maxed out — resetting"))
        }
        if (count >= 10) dispatch(CounterAction.Reset)
    }
}

// --- Composable ------------------------------------------------------------

@Composable
fun CounterScreen() {
    val vm: CounterViewModel = viewModel { CounterViewModel() }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(vm.effects) { effect ->
        when (effect) {
            is CounterEffect.ShowToast ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1116)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BasicText(
                text = "Hello from MVI",
                style = TextStyle(color = Color(0xFFE6EDF3), fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            )
            BasicText(
                text = state.count.toString(),
                style = TextStyle(color = Color(0xFF7EE787), fontSize = 96.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(8.dp))
            CounterRow(
                onDecrement = { vm.dispatch(CounterAction.Decrement) },
                onIncrement = { vm.dispatch(CounterAction.Increment) },
                onReset = { vm.dispatch(CounterAction.Reset) },
            )
        }
    }
}

@Composable
private fun CounterRow(
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppButton(
            onClick = onDecrement,
            modifier = Modifier.pill(Color(0xFF21262D)),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
        ) {
            BasicText("−", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        }
        AppButton(
            onClick = onReset,
            modifier = Modifier.pill(Color(0xFF30363D)),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        ) {
            BasicText("Reset", style = TextStyle(color = Color.White, fontSize = 14.sp))
        }
        AppButton(
            onClick = onIncrement,
            modifier = Modifier.pill(Color(0xFF238636)),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
        ) {
            BasicText("+", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        }
    }
}

private fun Modifier.pill(color: Color): Modifier =
    this.clip(RoundedCornerShape(50)).background(color)
