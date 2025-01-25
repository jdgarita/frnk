package dev.jdgarita.frnk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform