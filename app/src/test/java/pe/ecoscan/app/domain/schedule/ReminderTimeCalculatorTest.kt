package pe.ecoscan.app.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.ecoscan.app.domain.model.CollectionSchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReminderTimeCalculatorTest {

    private val lima = ZoneId.of("America/Lima") // UTC-5, sin horario de verano

    private fun millisAt(date: LocalDate, time: LocalTime) =
        date.atTime(time).atZone(lima).toInstant().toEpochMilli()

    @Test
    fun `si el dia programado todavia no llego esta semana, la proxima ocurrencia es ese dia`() {
        val now = LocalDate.of(2026, 10, 19)
        val nowMillis = millisAt(now, LocalTime.of(10, 0))
        val targetDay = now.plusDays(3)
        val schedule = CollectionSchedule(district = "Test", dayOfWeek = targetDay.dayOfWeek, hour = 8)

        val delay = calculateInitialDelayMillis(schedule, nowMillis, lima)

        assertEquals(millisAt(targetDay, LocalTime.of(8, 0)) - nowMillis, delay)
    }

    @Test
    fun `si hoy es el dia programado pero la hora ya paso, la proxima ocurrencia es en 7 dias`() {
        val now = LocalDate.of(2026, 10, 19)
        val nowMillis = millisAt(now, LocalTime.of(10, 0))
        val schedule = CollectionSchedule(district = "Test", dayOfWeek = now.dayOfWeek, hour = 8)

        val delay = calculateInitialDelayMillis(schedule, nowMillis, lima)

        assertEquals(millisAt(now.plusDays(7), LocalTime.of(8, 0)) - nowMillis, delay)
    }

    @Test
    fun `si hoy es el dia programado y la hora todavia no llega, la proxima ocurrencia es hoy`() {
        val now = LocalDate.of(2026, 10, 19)
        val nowMillis = millisAt(now, LocalTime.of(6, 0))
        val schedule = CollectionSchedule(district = "Test", dayOfWeek = now.dayOfWeek, hour = 8)

        val delay = calculateInitialDelayMillis(schedule, nowMillis, lima)

        assertEquals(millisAt(now, LocalTime.of(8, 0)) - nowMillis, delay)
    }

    @Test
    fun `si el dia programado ya paso esta semana, la proxima ocurrencia es la semana siguiente`() {
        val now = LocalDate.of(2026, 10, 19)
        val nowMillis = millisAt(now, LocalTime.of(10, 0))
        val targetDay = now.minusDays(1)
        val schedule = CollectionSchedule(district = "Test", dayOfWeek = targetDay.dayOfWeek, hour = 8)

        val delay = calculateInitialDelayMillis(schedule, nowMillis, lima)

        assertEquals(millisAt(now.plusDays(6), LocalTime.of(8, 0)) - nowMillis, delay)
    }
}
