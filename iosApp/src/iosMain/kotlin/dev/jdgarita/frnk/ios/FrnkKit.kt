package dev.jdgarita.frnk.ios

import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.shared.initializeFrnk
import org.koin.core.KoinApplication

fun bootstrapFrnkKit(backend: BackendChoice = BackendChoice.Supabase): KoinApplication = initializeFrnk(backend)
