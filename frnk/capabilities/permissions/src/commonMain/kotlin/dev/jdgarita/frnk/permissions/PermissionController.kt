package dev.jdgarita.frnk.permissions

/**
 * Capability **scaffold** (restructure Stage 11 / D4): the minimal cross-platform runtime-permission
 * contract. Api-only for now — there is **no** `expect/actual` or native cinterop, only
 * [NoopPermissionController]. A real platform implementation is future feature work, out of scope.
 */
interface PermissionController {
    /** Current status of [permission] without prompting the user. */
    fun status(permission: Permission): PermissionStatus

    /** Request [permission] from the user, returning the resulting status. */
    suspend fun request(permission: Permission): PermissionStatus
}

/** The runtime permissions the scaffold knows about. Extend as real impls land. */
enum class Permission {
    Camera,
    Microphone,
    Notifications,
    Location
}

/** Coarse permission state shared across platforms. */
enum class PermissionStatus {
    Granted,
    Denied,
    NotDetermined
}