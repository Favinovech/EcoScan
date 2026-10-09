package pe.ecoscan.app.data.scanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeDeduplicatorTest {

    @Test
    fun `el mismo codigo dentro de la ventana de 3 segundos no se emite dos veces`() {
        var now = 0L
        val deduplicator = BarcodeDeduplicator(clock = { now })

        assertTrue(deduplicator.shouldEmit("7501234567890"))

        now += 2_999L
        assertFalse(deduplicator.shouldEmit("7501234567890"))
    }

    @Test
    fun `el mismo codigo pasada la ventana de 3 segundos vuelve a emitirse`() {
        var now = 0L
        val deduplicator = BarcodeDeduplicator(clock = { now })

        assertTrue(deduplicator.shouldEmit("7501234567890"))

        now += 3_000L
        assertTrue(deduplicator.shouldEmit("7501234567890"))
    }

    @Test
    fun `codigos distintos se emiten de forma independiente`() {
        var now = 0L
        val deduplicator = BarcodeDeduplicator(clock = { now })

        assertTrue(deduplicator.shouldEmit("7501234567890"))
        assertTrue(deduplicator.shouldEmit("0000000000000"))
    }
}
