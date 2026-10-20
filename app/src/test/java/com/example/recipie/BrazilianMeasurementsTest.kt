package com.example.recipie

import com.example.recipie.data.translation.BrazilianMeasurements
import com.example.recipie.data.translation.CulinaryGlossary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BrazilianMeasurementsTest {
    private val units = BrazilianMeasurements()
    @Test fun distinguishesWeightAndLiquidOunces() {
        assertEquals("aproximadamente 28 g", units.convert("1 ounce"))
        assertEquals("aproximadamente 30 mL", units.convert("1 fluid ounce"))
        assertEquals("aproximadamente 237 mL", units.convert("8 fl. oz."))
    }
    @Test fun packageSizePreservesPackageCount() {
        assertEquals("2 (aproximadamente 411 g) cans", units.convert("2 (14.5 ounce) cans"))
    }
    @Test fun handlesFractionsRangesAndLargeAmounts() {
        assertEquals("aproximadamente 680 g", units.convert("1 1/2 pounds"))
        assertEquals("aproximadamente 680 g", units.convert("1 ½ pounds"))
        assertEquals("aproximadamente 227 g", units.convert("½ lb"))
        assertEquals("aproximadamente 14 g", units.convert("1/2 oz"))
        assertEquals("aproximadamente 227 a 340 g", units.convert("8 to 12 ounces"))
        assertEquals("aproximadamente 2,84 L", units.convert("3 quarts"))
        assertEquals("aproximadamente 1,36 kg", units.convert("3 pounds"))
    }
    @Test fun convertsTemperatureAndPanDimensionsWithoutChangingMetric() {
        assertEquals("aproximadamente 177 °C", units.convert("350 degrees F"))
        assertEquals("aproximadamente -40 °C", units.convert("-40°F"))
        assertEquals("aproximadamente 22,86 cm", units.convert("9-inch"))
        assertEquals("200 g, 30 mL, 180 °C, 2 cups", units.convert("200 g, 30 mL, 180 °C, 2 cups"))
    }
    @Test fun nestedProtectionKeepsNamesAndMeasurements() = runBlocking {
        val glossary = CulinaryGlossary(mapOf("air fryer" to "AirFryer"))
        val result = units.translate("Cook 8 ounces in the air fryer at 350°F.") { source ->
            glossary.translate(source) { it.replace("Cook", "Cozinhe").replace("in the", "na").replace("at", "a") }
        }
        assertTrue(result.contains("227 g")); assertTrue(result.contains("177 °C"))
        assertTrue(result.contains("AirFryer")); assertFalse(result.contains("ZXQ")); assertFalse(result.contains("ZWM"))
    }
    @Test fun alteredTokensStillPreserveConvertedAmount() = runBlocking {
        val result = units.translate("Use 8 ounces.") { if (it.contains("ZWM")) "Token perdido" else it.replace("Use", "Use") }
        assertTrue(result.contains("227 g")); assertFalse(result.contains("ZWM"))
    }
}
