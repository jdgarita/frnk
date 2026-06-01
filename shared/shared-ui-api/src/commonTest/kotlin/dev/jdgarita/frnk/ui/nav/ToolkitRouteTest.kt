package dev.jdgarita.frnk.ui.nav

import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `ToolkitRoute` is `@Serializable` so navigation-compose can use it as a type-safe destination. The
 * real encode/decode runs against the SavedState runtime (not unit-testable here), so this guards the
 * two things that matter at the contract level: every member resolves a serializer, and each member's
 * `serialName` is stable + distinct (a silent rename would break back-stack restoration / deep links).
 */
class ToolkitRouteTest {
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
