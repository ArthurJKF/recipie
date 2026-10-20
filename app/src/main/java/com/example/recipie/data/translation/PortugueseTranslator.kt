package com.example.recipie.data.translation

import android.content.Context
import org.json.JSONObject
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class PortugueseTranslator(context: Context) {
    private val glossary = context.assets.open("translation/culinary_glossary.json").bufferedReader().use { reader ->
        val json = JSONObject(reader.readText())
        fun map(name: String): Map<String, String> {
            val objectValue = json.getJSONObject(name)
            return objectValue.keys().asSequence().associateWith { objectValue.getString(it) }
        }
        CulinaryGlossary(map("preserve") + map("culinary"), map("simplify"), map("repair"))
    }
    private val client = Translation.getClient(TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.PORTUGUESE).build())
    private val measurements = BrazilianMeasurements()
    private val mutex = Mutex()
    private var ready = false

    suspend fun prepare() = mutex.withLock {
        if (!ready) {
            withTimeout(180_000) { client.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build()).await() }
            ready = true
        }
    }

    suspend fun translate(text: String?): String? {
        if (text.isNullOrBlank()) return null
        prepare()
        // Evita cortes em textos longos; os parágrafos e a ordem das etapas são mantidos.
        val chunks = text.split(Regex("(?<=[.!?;])\\s+|\\n+")).filter { it.isNotBlank() }
        val result = mutableListOf<String>()
        for (chunk in chunks) {
            if (chunk.length <= 3500) result += mutex.withLock { withTimeout(60_000) { translateChunk(chunk) } }
            else {
                val words = chunk.split(' ')
                var part = ""
                for (word in words) {
                    if (part.length + word.length > 3500) {
                        result += mutex.withLock { translateChunk(part) }; part = ""
                    }
                    part += if (part.isEmpty()) word else " $word"
                }
                if (part.isNotEmpty()) result += mutex.withLock { translateChunk(part) }
            }
        }
        return result.joinToString("\n\n")
    }
    private suspend fun translateChunk(text: String): String = measurements.translate(text) { measured ->
        glossary.translate(measured) { client.translate(it).await() }
    }
    fun close() { client.close() }
}
