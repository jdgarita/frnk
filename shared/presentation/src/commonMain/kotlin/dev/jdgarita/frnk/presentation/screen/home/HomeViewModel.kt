package dev.jdgarita.frnk.presentation.screen.home

import androidx.lifecycle.viewModelScope
import com.tweener.kmpkit.kotlinextensions.onInit
import dev.jdgarita.frnk.domain.usecase.user.FetchUserUseCase
import dev.jdgarita.frnk.presentation._internal.viewmodel.ViewModel
import dev.jdgarita.frnk.presentation.screen.home.model.HomeUiAction
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @author Vivien Mahe
 * @since 14/02/2024
 */
class HomeViewModel(
    private val fetchUserUseCase: FetchUserUseCase,
) : ViewModel() {

    // region Observable properties

    private val _openDetailScreen = MutableSharedFlow<String>() // id
    val openDetailScreen: SharedFlow<String> = _openDetailScreen.asSharedFlow()

    // endregion Observable properties

    private var fetchUserDone: Boolean = false

    private val initialize = viewModelScope.onInit { loadData() }

    fun onUiAction(uiAction: HomeUiAction) {
        Napier.d { "UiAction received: $uiAction" }

        when (uiAction) {
            is HomeUiAction.OpenDetailScreenClick -> onDetailButtonClicked()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Fetch the latest user data (in case changes happened from Firestore) only once
            if (fetchUserDone.not()) {
                fetchUserDone = true
                fetchUserUseCase.execute()
            }
        }
    }

    private fun onDetailButtonClicked() {
        viewModelScope.launch {
            _openDetailScreen.emit("1")
        }
    }
}
