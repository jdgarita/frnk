package dev.jdgarita.frnk.ui.app.shell

import androidx.navigation3.runtime.NavKey

/**
 * What [FrnkStackShell] hands its entries and sheet contents: the one back stack, and the sheets held
 * above it. Stable for the shell's lifetime, so it is safe to capture.
 */
interface FrnkShellNavigator {
    /** Push [route] onto the shell's back stack. Pushing the route already on top is a no-op. */
    fun push(route: NavKey)

    /** Pop the shell's back stack. A no-op at the start route: the stack is never emptied. */
    fun back()

    /** Raise the [FrnkShellSheet] whose key is [sheetKey], with fresh content. A no-op while it is up. */
    fun present(sheetKey: String)

    /** Take the sheet whose key is [sheetKey] down — also for a non-dismissible one: the host decided. */
    fun dismiss(sheetKey: String)
}