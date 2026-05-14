package dev.jdgarita.frnk.common

import kotlin.test.Test
import kotlin.test.assertEquals

class AppResultTest {
    @Test
    fun mapsSuccess() {
        val r: AppResult<Int, Nothing> = AppResult.Success(2)
        assertEquals(4, (r.map { it * 2 } as AppResult.Success).data)
    }
}
