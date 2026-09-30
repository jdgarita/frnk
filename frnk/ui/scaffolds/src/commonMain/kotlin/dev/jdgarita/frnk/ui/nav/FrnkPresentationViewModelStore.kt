package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Scopes `koinViewModel()` lookups inside [content] to this presentation instead of the host
 * Activity, and clears them when it leaves composition.
 *
 * Nav3 entries get this from the ViewModel-store entry decorator. Content presented outside the
 * back stack — a modal sheet — inherits the Activity's store instead, which would hand every
 * presentation the previous one's ViewModel (and its finished state). Wrapping restores the
 * fresh-state-per-presentation guarantee a nav destination has.
 *
 * Hosts key each presentation (`key(presentationCount) { … }`) so the store is replaced even when
 * a sheet re-opens before the previous one fully unmounts.
 */
@Composable
public fun FrnkPresentationViewModelStore(content: @Composable () -> Unit) {
    val owner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }

    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        content()
    }
}