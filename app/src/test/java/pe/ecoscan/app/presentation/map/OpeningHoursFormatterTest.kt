package pe.ecoscan.app.presentation.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class OpeningHoursFormatterTest {

    @Test
    fun `traduce los dias al espanol`() {
        assertEquals("Lun-Vie 09:00-18:00", formatOpeningHours("Mo-Fr 09:00-18:00"))
    }

    @Test
    fun `varios tramos se separan con un punto medio`() {
        assertEquals(
            "Lun-Vie 09:00-18:00 · Sáb 09:00-13:00",
            formatOpeningHours("Mo-Fr 09:00-18:00; Sa 09:00-13:00")
        )
    }

    @Test
    fun `24 7 se muestra como abierto las 24 horas`() {
        assertEquals("Abierto las 24 horas", formatOpeningHours("24/7"))
    }

    @Test
    fun `off y feriados se traducen`() {
        assertEquals("Lun-Sáb 08:00-17:00 · Dom cerrado", formatOpeningHours("Mo-Sa 08:00-17:00; Su off"))
        assertEquals("feriados cerrado", formatOpeningHours("PH off"))
    }

    @Test
    fun `un horario vacio o nulo devuelve null`() {
        assertNull(formatOpeningHours(null))
        assertNull(formatOpeningHours("   "))
    }

    @Test
    fun `una distancia menor a un kilometro se muestra en metros`() {
        assertEquals("350 m", formatDistance(350, Locale.US))
        assertEquals("999 m", formatDistance(999, Locale.US))
    }

    @Test
    fun `una distancia desde un kilometro se muestra en kilometros con un decimal`() {
        assertEquals("1.0 km", formatDistance(1_000, Locale.US))
        assertEquals("1.3 km", formatDistance(1_250, Locale.US))
    }
}