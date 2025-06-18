package com.tweener.kmpship.presentation.screen.detail.mapper

import com.tweener.kmpship.presentation.model.ToastMessage

/**
 * @author Vivien Mahe
 * @since 15/03/2024
 */
sealed class DetailToastMessage : ToastMessage() {

    data class LoadData(val id: String) : DetailToastMessage()

}
