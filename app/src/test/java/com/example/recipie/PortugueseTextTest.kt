package com.example.recipie

import com.example.recipie.data.model.PortugueseText
import org.junit.Assert.assertEquals
import org.junit.Test

class PortugueseTextTest {
    @Test fun searchAcceptsAccentsAndUppercase() {
        assertEquals("acucar", PortugueseText.key(" AÇÚCAR "))
        assertEquals("oleo de soja", PortugueseText.key("Óleo de soja"))
        assertEquals("limao-siciliano", PortugueseText.key("Limão-siciliano"))
    }
    @Test fun durationsUsePortugueseUnits() {
        assertEquals("1 h 20 min", PortugueseText.time("1 hr 20 mins"))
        assertEquals("2 dias", PortugueseText.time("2 days"))
        assertEquals("5 min", PortugueseText.time("5 minutes"))
        assertEquals(null, PortugueseText.time(null))
    }
}
