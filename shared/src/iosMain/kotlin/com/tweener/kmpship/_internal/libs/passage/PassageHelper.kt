package com.tweener.kmpship._internal.libs.passage

import com.tweener.passage.Passage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Vivien Mahe
 * @since 15/04/2025
 */
class PassageHelper : KoinComponent {

    private val passage: Passage by inject()

    fun handle(url: String): Boolean =
        passage.handleLink(url = url)
}
