package com.example.recipie.data.model

import java.text.Normalizer
import java.util.Locale

object PortugueseText {
    fun key(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
        .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
        .lowercase(Locale.ROOT).trim()

    fun time(text: String?): String? = text?.let {
        it.replace(Regex("""(?i)\b(?:hours?|hrs?)\b"""), "h")
            .replace(Regex("""(?i)\b(?:minutes?|mins?)\b"""), "min")
            .replace(Regex("""(?i)\b(?:seconds?|secs?)\b"""), "s")
            .replace(Regex("""(?i)\bdays?\b"""), "dias")
    }
}
