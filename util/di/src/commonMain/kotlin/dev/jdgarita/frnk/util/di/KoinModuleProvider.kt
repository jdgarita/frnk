package dev.jdgarita.frnk.util.di

import org.koin.core.module.Module

interface KoinModuleProvider {
    val modules: List<Module>
}