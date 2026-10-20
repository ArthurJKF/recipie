package com.example.recipie.data.translation

/** Aplica termos culinários e impede a tradução de nomes próprios. */
class CulinaryGlossary(
    private val terms: Map<String, String>,
    private val simplifications: Map<String, String> = emptyMap(),
    private val repairs: Map<String, String> = emptyMap(),
    private val tokenPrefix: String = "ZXQ"
) {
    data class Protected(val text: String, val tokens: Map<String, String>, val source: String)
    private val matcher = if (terms.isEmpty()) null else Regex(
        "(?<![\\p{L}\\p{N}])(?:" + terms.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) } + ")(?![\\p{L}\\p{N}])",
        RegexOption.IGNORE_CASE)
    private val normalizedTerms = terms.mapKeys { it.key.lowercase(java.util.Locale.ROOT) }

    fun simplify(text: String): String = simplifications.entries.sortedByDescending { it.key.length }
        .fold(text) { current, (source, target) ->
            current.replace(Regex("(?<![\\p{L}\\p{N}])" + Regex.escape(source) + "(?![\\p{L}\\p{N}])", RegexOption.IGNORE_CASE), target)
        }

    fun protect(text: String): Protected {
        val source = text
        val tokens = linkedMapOf<String, String>()
        val shielded = matcher?.replace(source) { match ->
            // Tokens sem significado; se o tradutor os alterar, usamos a alternativa por segmentos.
            val token = "${tokenPrefix}${tokens.size}QXZ"
            tokens[token] = normalizedTerms.getValue(match.value.lowercase(java.util.Locale.ROOT))
            token
        } ?: source
        return Protected(simplify(shielded), tokens, source)
    }

    fun restore(translated: String, protected: Protected): String? {
        var text = translated
        for ((token, replacement) in protected.tokens) {
            val pattern = Regex(token, RegexOption.IGNORE_CASE)
            if (pattern.findAll(text).count() != 1) return null
            text = text.replace(pattern) { replacement }
        }
        return polish(text)
    }

    suspend fun translate(text: String, engine: suspend (String) -> String): String {
        val protected = protect(text)
        val translated = engine(protected.text)
        restore(translated, protected)?.let { return it }
        // Garante que nomes protegidos nunca sejam substituídos por uma tradução inventada.
        val parts = mutableListOf<String>()
        var end = 0
        for (match in matcher?.findAll(protected.source).orEmpty()) {
            val before = protected.source.substring(end, match.range.first)
            if (before.isNotBlank()) parts += engine(simplify(before)).trim() else if (before.isNotEmpty()) parts += before
            parts += normalizedTerms.getValue(match.value.lowercase(java.util.Locale.ROOT))
            end = match.range.last + 1
        }
        val tail = protected.source.substring(end)
        if (tail.isNotBlank()) parts += engine(simplify(tail)).trim() else if (tail.isNotEmpty()) parts += tail
        return polish(parts.joinToString(" ").replace(Regex("\\s+([,.;:!?])"), "$1").trim())
    }

    private fun polish(value: String): String = repairs.entries.fold(value) { text, (source, target) ->
        text.replace(Regex(Regex.escape(source), RegexOption.IGNORE_CASE), target)
    }
}
