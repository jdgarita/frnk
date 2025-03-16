package dev.jdgarita.frnk.domain.config

const val FRNK_VERSION = "0.01"

data class FrnkConfiguration(val platformVersion: String = FRNK_VERSION)