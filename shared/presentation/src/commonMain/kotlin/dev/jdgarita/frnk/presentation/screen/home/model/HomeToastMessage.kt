package dev.jdgarita.frnk.presentation.screen.home.model

import dev.jdgarita.frnk.presentation.model.ToastMessage

/**
 * @author Vivien Mahe
 * @since 19/02/2024
 */
sealed class HomeToastMessage : ToastMessage() {

    data class OpenScreen(val id: String) : HomeToastMessage()


}
