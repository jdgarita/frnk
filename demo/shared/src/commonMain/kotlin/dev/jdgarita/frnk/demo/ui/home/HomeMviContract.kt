package dev.jdgarita.frnk.demo.ui.home

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.monetization.ProSource
import dev.jdgarita.frnk.ui.mvi.Arguments
import dev.jdgarita.frnk.ui.mvi.ModelState
import dev.jdgarita.frnk.ui.mvi.ModelStateFactory
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.ui.scaffolds.home.HomeScreenState

@Immutable
data class DemoHomeScreenState(
    val count: Int = 0,
    val email: String = "",
    val isPro: Boolean = false,
    val isGodMode: Boolean = false,
    // Where Pro comes from (None / Purchase / GodMode) — shown in the FeatureGate demo section.
    val proSource: String = ProSource.None.name,
    val notes: List<String> = emptyList(),
    // components tab — search state (which-screen-is-shown is owned by the nav back stack, not here)
    val searchActive: Boolean = false,
    val searchQuery: String = "",
    // components gallery widget demos
    val gallerySwitchOn: Boolean = true,
    val gallerySegmentIndex: Int = 0,
    val galleryNavIndex: Int = 0,
    // Stage 11 capability scaffolds — Remote Config value + camera/permission no-op outcomes.
    val remoteWelcome: String = "",
    val cameraResult: String = "Not captured",
    val cameraPermission: String = "",
    val frnkHomeState: HomeScreenState
) : UiState

@Immutable
data class DemoHomeModelState(
    val count: Int = 0,
    val email: String = "",
    val isPro: Boolean = false,
    val isGodMode: Boolean = false,
    // Where Pro comes from (None / Purchase / GodMode) — shown in the FeatureGate demo section.
    val proSource: String = ProSource.None.name,
    val notes: List<String> = emptyList(),
    // components tab — search state (which-screen-is-shown is owned by the nav back stack, not here)
    val searchActive: Boolean = false,
    val searchQuery: String = "",
    // components gallery widget demos
    val gallerySwitchOn: Boolean = true,
    val gallerySegmentIndex: Int = 0,
    val galleryNavIndex: Int = 0,
    // Stage 11 capability scaffolds — Remote Config value + camera/permission no-op outcomes.
    val remoteWelcome: String = "",
    val cameraResult: String = "Not captured",
    val cameraPermission: String = ""
) : ModelState

sealed interface DemoHomeIntent : UiIntent {
    data object Increment : DemoHomeIntent

    data object Decrement : DemoHomeIntent

    data class EmailChanged(
        val value: String
    ) : DemoHomeIntent

    data object ToggleGodMode : DemoHomeIntent

    data object RefreshEntitlements : DemoHomeIntent

    data object RestorePurchases : DemoHomeIntent

    data object RequestUpgrade : DemoHomeIntent

    data object AddNote : DemoHomeIntent

    data object ClearNotes : DemoHomeIntent

    data object TrackEvent : DemoHomeIntent

    data object SetUserProperty : DemoHomeIntent

    data object LogBreadcrumb : DemoHomeIntent

    data object RecordTestCrash : DemoHomeIntent

    data object ForceUnhandledCrash : DemoHomeIntent

    // components tab — search
    data object SearchOpened : DemoHomeIntent

    data object SearchClosed : DemoHomeIntent

    data class SearchQueryChanged(
        val value: String
    ) : DemoHomeIntent

    // components gallery widget demos
    data class GallerySwitchChanged(
        val checked: Boolean
    ) : DemoHomeIntent

    data class GallerySegmentChanged(
        val index: Int
    ) : DemoHomeIntent

    data class GalleryNavChanged(
        val index: Int
    ) : DemoHomeIntent

    // Stage 11 capability scaffolds.
    data object FetchRemoteConfig : DemoHomeIntent

    data object CapturePhoto : DemoHomeIntent

    data object RequestCameraPermission : DemoHomeIntent
}

@Immutable
data object DemoHomeArguments : Arguments

sealed interface DemoHomeEffect : UiEffect {
    data class Navigate(
        val routeKey: String
    ) : DemoHomeEffect

    data class Toast(
        val message: String
    ) : DemoHomeEffect
}

object DemoHomeModelStateFactory : ModelStateFactory<DemoHomeModelState> {
    override fun initialModelState() = DemoHomeModelState()
}