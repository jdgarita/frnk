package dev.jdgarita.frnk.presentation.screen

import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.tab.Tab
import dev.jdgarita.frnk.domain.usecase.config.LoadAppConfigUseCase
import dev.jdgarita.frnk.presentation._internal.navigation.tab.FavoritesTab
import dev.jdgarita.frnk.presentation._internal.navigation.tab.HomeTab
import dev.jdgarita.frnk.presentation._internal.navigation.tab.ProfileTab
import dev.jdgarita.frnk.presentation._internal.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @author Vivien Mahe
 * @since 10/02/2024
 */
class MainViewModel(
    private val loadAppConfigUseCase: LoadAppConfigUseCase,
) : ViewModel() {

    // region Observable properties

    private val _navigationItems = MutableStateFlow(listOf(HomeTab, FavoritesTab, ProfileTab))
    val navigationItems: StateFlow<List<Tab>> = _navigationItems

    // endregion Observable properties

    fun initViewModel() {
        viewModelScope.launch {
            // Load all required app configuration
            loadAppConfigUseCase.execute()
        }
    }
}
