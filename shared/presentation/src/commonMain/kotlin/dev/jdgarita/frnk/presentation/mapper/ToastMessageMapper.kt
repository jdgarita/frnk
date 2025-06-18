package dev.jdgarita.frnk.presentation.mapper

import dev.jdgarita.frnk.presentation.model.ToastMessage
import dev.jdgarita.frnk.presentation.screen.detail.mapper.DetailToastMessage
import frnk.shared.presentation.generated.resources.Res
import frnk.shared.presentation.generated.resources.detail_load_data
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString

/**
 * @author Vivien Mahe
 * @since 29/12/2023
 */
class ToastMessageMapper : EntityToUiModelMapper<ToastMessage, String> {

    @OptIn(ExperimentalResourceApi::class)
    override fun convertToUiModel(entity: ToastMessage): String =
        runBlocking {
            when (entity) {
                is DetailToastMessage.LoadData -> getString(resource = Res.string.detail_load_data, entity.id)
                else -> ""
            }
        }
}
