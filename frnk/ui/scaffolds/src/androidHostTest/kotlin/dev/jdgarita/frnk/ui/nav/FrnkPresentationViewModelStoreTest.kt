package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jdgarita.frnk.ui.atoms.RobolectricComposeTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FrnkPresentationViewModelStoreTest : RobolectricComposeTest() {
    class Probe : ViewModel() {
        var cleared = false

        override fun onCleared() {
            cleared = true
        }
    }

    @Test
    fun each_presentation_gets_a_fresh_view_model_and_the_old_one_is_cleared() =
        runComposeUiTest {
            var presentation by mutableStateOf(1)
            val seen = mutableListOf<Probe>()
            setContent {
                androidx.compose.runtime.key(presentation) {
                    FrnkPresentationViewModelStore { seen += viewModel { Probe() } }
                }
            }
            waitForIdle()
            presentation = 2
            waitForIdle()
            val first = seen.first()
            val last = seen.last()
            assertNotSame(first, last)
            assertTrue(first.cleared)
        }
}