package pe.ecoscan.app.presentation.history

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class HistoryDateRangesTest {

    private val lima = ZoneId.of("America/Lima") // UTC-5, sin horario de verano

    private fun LocalDate.utcMidnightMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    private fun LocalDate.limaStartMillis() = atStartOfDay(lima).toInstant().toEpochMilli()

    @Test
    fun `rango del selector pasa a dias completos en la zona local`() {
        val start = LocalDate.of(2026, 10, 1)
        val end = LocalDate.of(2026, 10, 3)

        val range = pickerRangeToLocal(start.utcMidnightMillis(), end.utcMidnightMillis(), lima)

        assertEquals(start.limaStartMillis(), range.fromMillis)
        // Termina el 3 a las 23:59:59.999 hora de Lima
        assertEquals(LocalDate.of(2026, 10, 4).limaStartMillis() - 1, range.toMillis)
    }

    @Test
    fun `si solo se elige un dia el rango es ese dia`() {
        val day = LocalDate.of(2026, 10, 5)

        val range = pickerRangeToLocal(day.utcMidnightMillis(), null, lima)

        assertEquals(day.limaStartMillis(), range.fromMillis)
        assertEquals(day.plusDays(1).limaStartMillis() - 1, range.toMillis)
    }

    @Test
    fun `si las fechas vienen invertidas se ordenan`() {
        val first = LocalDate.of(2026, 10, 1)
        val last = LocalDate.of(2026, 10, 3)

        val range = pickerRangeToLocal(last.utcMidnightMillis(), first.utcMidnightMillis(), lima)

        assertEquals(first.limaStartMillis(), range.fromMillis)
        assertEquals(last.plusDays(1).limaStartMillis() - 1, range.toMillis)
    }

    @Test
    fun `ultimos 7 dias cuenta hoy y empieza a las 00 00 de hace 6 dias`() {
        val now = LocalDate.of(2026, 10, 10).atTime(15, 30).atZone(lima).toInstant().toEpochMilli()

        val range = lastDaysRange(7, now, lima)

        assertEquals(LocalDate.of(2026, 10, 4).limaStartMillis(), range.fromMillis)
        assertEquals(now, range.toMillis)
    }
}