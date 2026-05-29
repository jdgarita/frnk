package dev.jdgarita.frnk.demo

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.NoteStore
import dev.jdgarita.frnk.monetization.Feature
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.utils.fold
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DemoState(
    val count: Int = 0,
    val email: String = "",
    val isPro: Boolean = false,
    val notes: List<String> = emptyList(),
) : UiState

sealed interface DemoIntent : UiIntent {
    data object Increment : DemoIntent

    data object Decrement : DemoIntent

    data class EmailChanged(
        val value: String,
    ) : DemoIntent

    data object TogglePro : DemoIntent

    data object RequestUpgrade : DemoIntent

    data object AddNote : DemoIntent

    data object ClearNotes : DemoIntent
}

sealed interface DemoEffect : UiEffect {
    data class Navigate(
        val routeKey: String,
    ) : DemoEffect

    data class Toast(
        val message: String,
    ) : DemoEffect
}

class DemoViewModel(
    private val gate: FeatureGate,
    private val analytics: AnalyticsTracker,
    private val entitlements: FakeEntitlementManager,
    private val notes: NoteStore,
) : MviViewModel<DemoState, DemoIntent, DemoEffect>(DemoState()) {
    init {
        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "demo"))
        gate.isPro
            .onEach { pro -> setState { copy(isPro = pro) } }
            .launchIn(viewModelScope)
        viewModelScope.launch { loadNotes() }
    }

    override suspend fun onIntent(intent: DemoIntent) {
        when (intent) {
            DemoIntent.Increment -> setState { copy(count = count + 1) }
            DemoIntent.Decrement -> setState { copy(count = count - 1) }
            is DemoIntent.EmailChanged -> setState { copy(email = intent.value) }
            DemoIntent.TogglePro -> entitlements.setPro(!currentState().isPro)
            DemoIntent.RequestUpgrade -> {
                if (gate.canUse(Feature.Premium)) {
                    emit(DemoEffect.Toast("Already Pro — feature unlocked"))
                } else {
                    val route = gate.requestUpgrade(source = "demo_button")
                    emit(DemoEffect.Navigate(route))
                }
            }
            DemoIntent.AddNote ->
                notes
                    .add("Note #${currentState().notes.size + 1}")
                    .fold(
                        onSuccess = { loadNotes() },
                        onFailure = { emit(DemoEffect.Toast("Couldn't save note: ${it.message}")) },
                    )
            DemoIntent.ClearNotes ->
                notes.clear().fold(
                    onSuccess = { loadNotes() },
                    onFailure = { emit(DemoEffect.Toast("Couldn't clear notes: ${it.message}")) },
                )
        }
    }

    private suspend fun loadNotes() {
        notes.all().fold(
            onSuccess = { stored -> setState { copy(notes = stored.map { it.content }) } },
            onFailure = { emit(DemoEffect.Toast("Couldn't load notes: ${it.message}")) },
        )
    }
}
