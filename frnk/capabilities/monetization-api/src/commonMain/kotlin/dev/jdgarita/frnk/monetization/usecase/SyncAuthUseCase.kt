package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError

interface SyncAuthUseCase {
    /**
     * Ensures the anonymous identity exists and is identified with the billing backend, so a
     * request never leaves the device with a uid the billing backend doesn't know. Failure of
     * either step is a [AppResult.Failure]; callers that gate on the sync stop there, best-effort
     * callers (e.g. a bootstrap warmup) ignore the result.
     */
    suspend fun identify(): AppResult<Unit, CommonError>
}