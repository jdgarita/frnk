package dev.jdgarita.frnk.presentation._internal.launcher

/**
 * @author Vivien Mahe
 * @since 09/02/2025
 */

interface EmailComposerLauncher {

    fun composeEmail(recipients: List<String>, subject: String, body: String)

}
