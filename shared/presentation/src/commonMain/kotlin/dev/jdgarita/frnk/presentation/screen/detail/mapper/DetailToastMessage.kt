package dev.jdgarita.frnk.presentation.screen.detail.mapper

import dev.jdgarita.frnk.presentation.model.ToastMessage

/**
 * @author Vivien Mahe
 * @since 15/03/2024
 */
sealed class DetailToastMessage : ToastMessage() {

    data class LoadData(val id: String) : DetailToastMessage()

}
