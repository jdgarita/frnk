package dev.jdgarita.frnk.permissions

/**
 * SDK-free default [PermissionController]: [status] always reports [PermissionStatus.NotDetermined]
 * and [request] resolves to [PermissionStatus.Denied] (nothing is actually requested). Lets a host
 * compile + run against the contract before a real platform impl exists; demos surface this as the
 * honest "not wired" outcome.
 */
class NoopPermissionController : PermissionController {
    override fun status(permission: Permission): PermissionStatus = PermissionStatus.NotDetermined

    override suspend fun request(permission: Permission): PermissionStatus = PermissionStatus.Denied
}
