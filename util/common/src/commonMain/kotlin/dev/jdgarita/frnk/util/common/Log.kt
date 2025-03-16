package dev.jdgarita.frnk.util.common

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter

var Log = Logger(
    config = loggerConfigInit(
        platformLogWriter(DefaultFormatter),
        minSeverity = Severity.Verbose
    ),
    tag = ""
)

fun Logger.wCompat(msg: String, attrs: Map<String, String>) = w(msg + attributesToString(attrs))

fun Logger.eCompat(e: Throwable, attrs: Map<String, String>) = e(attributesToString(attrs), e)

fun Logger.attributesToString(attr: Map<String, String>): String = attr.entries.joinToString(" ")

fun Logger.configureLog(config: LoggerConfig, tag: String) {
    Log = Logger(config, tag)
}

const val TAG_TIMING: String = "Timing"