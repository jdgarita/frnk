package dev.jdgarita.frnk.presentation._internal.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * @author Vivien Mahe
 * @since 14/11/2024
 */

interface EmailClientLauncher {

    @Composable
    fun openEmailClient()
}

@Composable
fun rememberEmailClientLauncher(): EmailClientLauncher =
    remember {
        createEmailClientLauncher()
    }

expect fun createEmailClientLauncher(): EmailClientLauncher
