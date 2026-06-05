package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * `ToolkitRoute` is a `@Serializable` [NavKey] so Navigation3 can use it as a type-safe back-stack
 * destination. The real encode/decode runs against the SavedState runtime (not unit-testable here), so
 * this guards the things that matter at the contract level: every member is a `NavKey`, resolves a
 * serializer, and each member's `serialName` is stable + distinct (a silent rename would break
 * back-stack restoration / deep links).
 */
class ToolkitRouteTest {
    @Test
    fun members_are_nav_keys() {
        assertIs<NavKey>(ToolkitRoute.Home)
        assertIs<NavKey>(ToolkitRoute.Settings)
        assertIs<NavKey>(ToolkitRoute.Paywall)
        assertIs<NavKey>(ToolkitRoute.SignIn)
        assertIs<NavKey>(ToolkitRoute.SignUp)
        assertIs<NavKey>(ToolkitRoute.Custom(id = "x"))
    }

    @Test
    fun sealed_route_resolves_a_serializer() {
        assertEquals(
            "dev.jdgarita.frnk.ui.nav.ToolkitRoute",
            serializer<ToolkitRoute>().descriptor.serialName,
        )
    }

    @Test
    fun each_member_has_a_stable_distinct_serial_name() {
        val names =
            listOf(
                serializer<ToolkitRoute.Home>().descriptor.serialName,
                serializer<ToolkitRoute.Settings>().descriptor.serialName,
                serializer<ToolkitRoute.Paywall>().descriptor.serialName,
                serializer<ToolkitRoute.SignIn>().descriptor.serialName,
                serializer<ToolkitRoute.SignUp>().descriptor.serialName,
                serializer<ToolkitRoute.Custom>().descriptor.serialName,
            )

        assertEquals(names.size, names.toSet().size, "route serial names must be distinct")
        assertTrue(names.all { it.startsWith("dev.jdgarita.frnk.ui.nav.ToolkitRoute") })
        assertEquals("dev.jdgarita.frnk.ui.nav.ToolkitRoute.Custom", names.last())
    }
}
