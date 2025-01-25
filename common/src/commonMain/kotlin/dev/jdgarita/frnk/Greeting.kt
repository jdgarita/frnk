package dev.jdgarita.frnk

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name} from Frnk framework!"
    }
}