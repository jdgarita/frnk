package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behavior contract for the back-stack mutation helpers. `NavBackStack` is a plain `MutableList<NavKey>`,
 * so these only need a constructed stack (no Compose/SavedState runtime). The single-top guard on
 * [navigateTo] is the load-bearing one — a doubly-fired navigation effect must not stack a duplicate.
 */
class NavBackStackExtTest {
    @Serializable
    private data object A : NavKey

    @Serializable
    private data object B : NavKey

    @Serializable
    private data class Detail(
        val id: String,
    ) : NavKey

    private fun stackOf(vararg start: NavKey) = NavBackStack(*start)

    @Test
    fun navigateTo_pushes_onto_the_stack() {
        val stack = stackOf(A)
        stack.navigateTo(B)
        assertEquals(listOf(A, B), stack.toList())
    }

    @Test
    fun navigateTo_is_single_top_by_default() {
        val stack = stackOf(A)
        stack.navigateTo(A)
        assertEquals(listOf(A), stack.toList(), "pushing the current top again is a no-op by default")
    }

    @Test
    fun navigateTo_single_top_only_compares_the_top_entry() {
        // A duplicate that isn't on top is still pushed (single-top guards the top, not the whole stack).
        val stack = stackOf(A, B)
        stack.navigateTo(A)
        assertEquals(listOf(A, B, A), stack.toList())
    }

    @Test
    fun navigateTo_single_top_distinguishes_distinct_instances_of_the_same_type() {
        val stack = stackOf(Detail("a"))
        stack.navigateTo(Detail("b"))
        assertEquals(listOf(Detail("a"), Detail("b")), stack.toList())
    }

    @Test
    fun navigateTo_with_single_top_false_allows_a_duplicate_top() {
        val stack = stackOf(A)
        stack.navigateTo(A, singleTop = false)
        assertEquals(listOf(A, A), stack.toList())
    }

    @Test
    fun navigateTo_removes_popScreen_before_pushing() {
        val stack = stackOf(A, B)
        stack.navigateTo(screen = Detail("x"), popScreen = B)
        assertEquals(listOf(A, Detail("x")), stack.toList())
    }

    @Test
    fun back_pops_the_top_and_is_a_no_op_when_empty() {
        val stack = stackOf(A, B)
        stack.back()
        assertEquals(listOf(A), stack.toList())
        stack.back()
        assertTrue(stack.isEmpty())
        stack.back() // no throw on empty
        assertTrue(stack.isEmpty())
    }

    @Test
    fun clearAndNavigateTo_resets_to_a_single_entry() {
        val stack = stackOf(A, B, Detail("x"))
        stack.clearAndNavigateTo(B)
        assertEquals(listOf(B), stack.toList())
    }
}
