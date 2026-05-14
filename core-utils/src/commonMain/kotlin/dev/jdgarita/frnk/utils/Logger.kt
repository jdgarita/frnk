package dev.jdgarita.frnk.utils

/** Minimal multiplatform logger; host can replace via Koin if richer output is needed. */
interface Logger {
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

object PrintLogger : Logger {
    override fun d(tag: String, message: String, throwable: Throwable?) = log("D", tag, message, throwable)
    override fun i(tag: String, message: String, throwable: Throwable?) = log("I", tag, message, throwable)
    override fun w(tag: String, message: String, throwable: Throwable?) = log("W", tag, message, throwable)
    override fun e(tag: String, message: String, throwable: Throwable?) = log("E", tag, message, throwable)
    private fun log(level: String, tag: String, msg: String, t: Throwable?) {
        println("[$level] $tag: $msg${t?.let { " :: ${it.message}" } ?: ""}")
    }
}
