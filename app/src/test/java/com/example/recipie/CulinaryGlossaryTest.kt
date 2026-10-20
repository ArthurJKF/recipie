package com.example.recipie

import com.example.recipie.data.translation.CulinaryGlossary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CulinaryGlossaryTest {
    private val glossary = CulinaryGlossary(mapOf(
        "air fryer" to "AirFryer", "roux" to "roux", "Tabasco" to "Tabasco",
        "Dutch oven" to "panela de ferro", "bacon drippings" to "gordura do bacon",
        "until vegetables are tender" to "até os legumes ficarem macios"
    ), mapOf("tender" to "soft"))

    @Test fun namesAndCookingTermsSurviveTranslation() {
        val protected = glossary.protect("Use the air fryer with roux and Tabasco.")
        val translated = "Use a " + protected.tokens.keys.joinToString(" e ") + "."
        val restored = glossary.restore(translated, protected)!!
        assertTrue(restored.contains("AirFryer"))
        assertTrue(restored.contains("roux"))
        assertTrue(restored.contains("Tabasco"))
        assertFalse(restored.contains("ZXQ"))
    }
    @Test fun fullCookingPhraseTakesPriorityOverAmbiguousWord() {
        val protected = glossary.protect("Cook until vegetables are tender.")
        assertEquals(listOf("até os legumes ficarem macios"), protected.tokens.values.toList())
        assertFalse(protected.text.contains("soft"))
    }
    @Test fun alteredTokensTriggerSafeFallback() = runBlocking {
        var calls = 0
        val text = glossary.translate("Use the air fryer.") { source ->
            calls++
            if (source.contains("ZXQ")) "Tradução que perdeu o nome." else source.replace("Use the", "Use a")
        }
        assertTrue(calls > 1)
        assertTrue(text.contains("AirFryer"))
        assertFalse(text.contains("ZXQ"))
    }
    @Test fun partialWordsAreNotReplaced() {
        val protected = glossary.protect("air fryersmith and tenderloin")
        assertTrue(protected.tokens.isEmpty())
        assertTrue(protected.text.contains("tenderloin"))
    }
    @Test fun tokenCaseChangesAreAccepted() {
        val protected = glossary.protect("Dutch oven")
        assertEquals("panela de ferro", glossary.restore(protected.text.lowercase(), protected))
    }
}
