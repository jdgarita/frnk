package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * `FrnkTabRoute` is a `@Serializable` [NavKey] so Navigation3 can use it as a type-safe back-stack
 * destination. The real encode/decode runs against the SavedState runtime (not unit-testable here), so
 * this guards the things that matter at the contract level: every member is a `NavKey`, resolves a
 * serializer, and each member's `serialName` is stable + distinct (a silent rename would break
 * back-stack restoration / deep links).
 */
class FrnkTabRouteTest {
    @Test
    fun members_are_nav_keys() {
        assertIs<NavKey>(FrnkTabRoute.Home)
        assertIs<NavKey>(FrnkTabRoute.Settings)
        assertIs<NavKey>(FrnkTabRoute.Custom(id = "x"))
    }

    @Test
    fun sealed_route_resolves_a_serializer() {
        assertEquals(
            "dev.jdgarita.frnk.ui.nav.FrnkTabRoute",
            serializer<FrnkTabRoute>().descriptor.serialName
        )
    }

    @Test
    fun each_member_has_a_stable_distinct_serial_name() {
        val names =
            listOf(
                serializer<FrnkTabRoute.Home>().descriptor.serialName,
                serializer<FrnkTabRoute.Settings>().descriptor.serialName,
                serializer<FrnkTabRoute.Custom>().descriptor.serialName
            )

        assertEquals(names.size, names.toSet().size, "route serial names must be distinct")
        assertTrue(names.all { it.startsWith("dev.jdgarita.frnk.ui.nav.FrnkTabRoute") })
        assertEquals("dev.jdgarita.frnk.ui.nav.FrnkTabRoute.Custom", names.last())
    }
}