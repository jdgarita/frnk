package dev.jdgarita.frnk.util.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

internal object DateFormatSerializer : KSerializer<DateFormat> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = "DateFormat"
    ) {
        element("type", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: DateFormat) {
        encoder.encodeStructure(descriptor) {
            encodeType(value)
        }
    }

    private fun CompositeEncoder.encodeType(value: DateFormat) {
        encodeStringElement(descriptor, INDEX_OF_TYPE, value.toString())
    }

    override fun deserialize(decoder: Decoder): DateFormat = decoder.decodeStructure(descriptor) {
        val value = when (decodeElementIndex(descriptor)) {
            INDEX_OF_TYPE -> decodeStringElement(descriptor, INDEX_OF_TYPE)
            else -> "".also {
                Log.e { "Unexpected index when deserializing $this" }
            }
        }

        fromString(value)
    }

    private fun fromString(value: String): DateFormat = when (value) {
        "MonthDaySingleDigit" -> DateFormat.MonthDaySingleDigit
        "MonthDayDoubleDigit" -> DateFormat.MonthDayDoubleDigit
        "MonthDayYearSingleDigit" -> DateFormat.MonthDayYearSingleDigit
        "MonthDayYearDoubleDigit" -> DateFormat.MonthDayYearDoubleDigit
        "ThreeLetterMonthDoubleDigitDayFullYear" -> DateFormat.ThreeLetterMonthDoubleDigitDayFullYear
        "FullMonthDoubleDigitDayFullYear" -> DateFormat.FullMonthDoubleDigitDayFullYear
        "FullMonthDoubleDigitDay" -> DateFormat.FullMonthDoubleDigitDay
        else -> DateFormat.MonthDayDoubleDigit.also {
            Log.e { "Unknown DateFormat type: $value, falling back to default" }
        }
    }

    private const val INDEX_OF_TYPE = 0
}