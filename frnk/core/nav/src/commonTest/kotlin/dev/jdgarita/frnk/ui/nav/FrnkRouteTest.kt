package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * `FrnkRoute` is a `@Serializable` [NavKey] so Navigation3 can use it as a type-safe back-stack
 * destination. The real encode/decode runs against the SavedState runtime (not unit-testable here), so
 * this guards the things that matter at the contract level: every member is a `NavKey`, resolves a
 * serializer, and each member's `serialName` is stable + distinct (a silent rename would break
 * back-stack restoration / deep links).
 */
class FrnkRouteTest {
    @Test
    fun members_are_nav_keys() {
        assertIs<NavKey>(FrnkRoute.Home)
        assertIs<NavKey>(FrnkRoute.Onboarding)
        assertIs<NavKey>(FrnkRoute.Settings)
        assertIs<NavKey>(FrnkRoute.Paywall)
        assertIs<NavKey>(FrnkRoute.Custom(id = "x"))
    }

    @Test
    fun sealed_route_resolves_a_serializer() {
        assertEquals(
            "dev.jdgarita.frnk.ui.nav.FrnkRoute",
            serializer<FrnkRoute>().descriptor.serialName
        )
    }

    @Test
    fun each_member_has_a_stable_distinct_serial_name() {
        val names =
            listOf(
                serializer<FrnkRoute.Home>().descriptor.serialName,
                serializer<FrnkRoute.Onboarding>().descriptor.serialName,
                serializer<FrnkRoute.Settings>().descriptor.serialName,
                serializer<FrnkRoute.Paywall>().descriptor.serialName,
                serializer<FrnkRoute.Custom>().descriptor.serialName
            )

        assertEquals(names.size, names.toSet().size, "route serial names must be distinct")
        assertTrue(names.all { it.startsWith("dev.jdgarita.frnk.ui.nav.FrnkRoute") })
        assertEquals("dev.jdgarita.frnk.ui.nav.FrnkRoute.Custom", names.last())
    }
}