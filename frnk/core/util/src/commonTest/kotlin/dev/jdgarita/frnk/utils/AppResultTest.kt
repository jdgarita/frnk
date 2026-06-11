package dev.jdgarita.frnk.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppResultTest {
    @Test
    fun fold_runs_the_success_arm_for_Success() {
        val result: AppResult<Int, CommonError> = AppResult.Success(42)

        val folded =
            result.fold(
                onSuccess = { "ok:$it" },
                onFailure = { "err:${it.message}" },
            )

        assertEquals("ok:42", folded)
    }

    @Test
    fun fold_runs_the_failure_arm_for_Failure() {
        val result: AppResult<Int, CommonError> = AppResult.Failure(CommonError.Unauthorized)

        val folded =
            result.fold(
                onSuccess = { "ok:$it" },
                onFailure = { "err:${it.message}" },
            )

        assertEquals("err:Authentication required", folded)
    }

    @Test
    fun fold_only_invokes_the_matching_arm() {
        var successCalls = 0
        var failureCalls = 0

        AppResult.Success<Int>(1).fold(onSuccess = { successCalls++ }, onFailure = { failureCalls++ })

        assertEquals(1, successCalls)
        assertEquals(0, failureCalls)
    }

    @Test
    fun success_and_failure_carry_their_payloads() {
        val success: AppResult<String, CommonError> = AppResult.Success("data")
        val failure: AppResult<String, CommonError> = AppResult.Failure(CommonError.NotFound)

        assertTrue(success is AppResult.Success && success.data == "data")
        assertTrue(failure is AppResult.Failure && failure.error == CommonError.NotFound)
    }
}
