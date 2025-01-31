package dev.jdgarita.frnk.presentation.mvi

import org.koin.mp.KoinPlatform

class ViewModelProvider {
    inline fun <reified T : MviViewModel<*, *, *, *>> getViewModel(): T =
        KoinPlatform.getKoin().get()
}