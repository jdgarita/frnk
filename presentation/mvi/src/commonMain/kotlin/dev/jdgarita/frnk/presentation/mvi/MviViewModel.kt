package dev.jdgarita.frnk.presentation.mvi

import androidx.lifecycle.ViewModel

abstract class MviViewModel : ViewModel()

interface Intent

interface ExternalEvent : Arguments

interface Arguments

interface ViewState