package dev.jdgarita.frnk.ui.mvi.ext

internal suspend fun <T> kotlinx.coroutines.flow.Flow<T>.collect() = collect {}