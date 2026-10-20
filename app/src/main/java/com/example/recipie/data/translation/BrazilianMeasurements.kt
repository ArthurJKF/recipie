package com.example.recipie.data.translation

import java.util.Locale
import kotlin.math.round

/** Converte unidades US da base; xícaras/colheres permanecem medidas de volume. */
class BrazilianMeasurements {
    private val number = "(?:\\d+\\s+\\d+[/⁄]\\d+|\\d+[/⁄]\\d+|\\d+(?:[.,]\\d+)?(?:\\s*[¼½¾⅓⅔⅛⅜⅝⅞])?|[¼½¾⅓⅔⅛⅜⅝⅞])"
    private val units = "(?:fluid\\s+ounces?|fl\\.?\\s*oz\\.?|ounces?|oz\\.?|pounds?|lbs?\\.?|quarts?|qts?\\.?|pints?|pts?\\.?|gallons?|gals?\\.?|inches?|inch|degrees?\\s+F(?:ahrenheit)?|°\\s*F|Fahrenheit)"
    private val pattern = Regex("(?<![\\p{L}\\p{N}/])(-?$number)(?:\\s*(?:to|[-–])\\s*($number))?\\s*[- ]?\\s*($units)(?![\\p{L}\\p{N}])", RegexOption.IGNORE_CASE)
    private val fractions = mapOf('¼' to .25, '½' to .5, '¾' to .75, '⅓' to 1.0/3, '⅔' to 2.0/3, '⅛' to .125, '⅜' to .375, '⅝' to .625, '⅞' to .875)

    private fun amount(source: String): Double {
        val text = source.trim().replace('⁄', '/').replace(',', '.')
        fractions[text.last()]?.let { return (text.dropLast(1).trim().toDoubleOrNull() ?: 0.0) + it }
        if ('/' in text) {
            val parts = text.split(Regex("\\s+"))
            val ratio = parts.last().split('/')
            return (if (parts.size > 1) parts.first().toDouble() else 0.0) + ratio[0].toDouble() / ratio[1].toDouble()
        }
        return text.toDouble()
    }
    private fun format(value: Double, decimals: Int): String = String.format(Locale.US, "%.$decimals" + "f", value)
        .trimEnd('0').trimEnd('.').ifEmpty { "0" }.replace('.', ',')

    private fun conversion(value: Double, unit: String): Pair<Double, String> {
        val normalized = unit.lowercase(Locale.ROOT).replace(Regex("[.\\s]"), "")
        return when {
            normalized.startsWith("fluid") || normalized.startsWith("fl") -> value * 29.5735295625 to "mL"
            normalized.startsWith("ounce") || normalized.startsWith("oz") -> value * 28.349523125 to "g"
            normalized.startsWith("pound") || normalized.startsWith("lb") -> value * 453.59237 to "g"
            normalized.startsWith("quart") || normalized.startsWith("qt") -> value * 946.352946 to "mL"
            normalized.startsWith("pint") || normalized.startsWith("pt") -> value * 473.176473 to "mL"
            normalized.startsWith("gallon") || normalized.startsWith("gal") -> value * 3785.411784 to "mL"
            normalized.startsWith("inch") -> value * 2.54 to "cm"
            else -> (value - 32) * 5 / 9 to "°C"
        }
    }
    private fun replacement(match: MatchResult): String {
        val first = conversion(amount(match.groupValues[1]), match.groupValues[3])
        val second = match.groupValues[2].takeIf { it.isNotEmpty() }?.let { conversion(amount(it), match.groupValues[3]) }
        val large = first.second in listOf("g", "mL") && first.first >= 1000 && (second == null || second.first >= 1000)
        val unit = if (large) (if (first.second == "g") "kg" else "L") else first.second
        fun display(value: Double): String {
            val converted = if (large) value / 1000 else value
            return if (unit == "°C") round(converted).toInt().toString()
            else if (large || unit == "cm") format(converted, 2)
            else if (converted < 10) format(converted, 1)
            else round(converted).toInt().toString()
        }
        return "aproximadamente " + display(first.first) + (second?.let { " a " + display(it.first) } ?: "") + " " + unit
    }

    fun convert(text: String): String = pattern.replace(text) { replacement(it) }

    suspend fun translate(text: String, engine: suspend (String) -> String): String {
        val terms = pattern.findAll(text).map { it.value to replacement(it) }.toMap()
        // O mesmo mecanismo de proteção impede o tradutor de mudar quantidades já convertidas.
        return CulinaryGlossary(terms, tokenPrefix = "ZWM").translate(text, engine)
    }
}
