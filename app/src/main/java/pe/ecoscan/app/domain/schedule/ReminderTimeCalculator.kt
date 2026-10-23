package pe.ecoscan.app.domain.schedule

import pe.ecoscan.app.domain.model.CollectionSchedule
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

// Milisegundos desde "nowMillis" hasta la próxima vez que se cumplan el día de la semana y la
// hora de "schedule", en la zona indicada. Función pura (solo java.time, de la biblioteca
// estándar de Java) para poder probarla con JUnit normal, sin WorkManager ni Android.
//
// Nota: este valor es el "initialDelay" que se le pasa a WorkManager, pero WorkManager no
// garantiza ejecutar el trabajo justo en ese instante. Doze y el ahorro de batería pueden
// retrasar la ejecución real.
fun calculateInitialDelayMillis(
    schedule: CollectionSchedule,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): Long {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
    var next = now
        .with(TemporalAdjusters.nextOrSame(schedule.dayOfWeek))
        .withHour(schedule.hour)
        .withMinute(schedule.minute)
        .withSecond(0)
        .withNano(0)

    // Si el día coincide con hoy pero la hora ya pasó, la próxima ocurrencia es en 7 días.
    if (!next.isAfter(now)) {
        next = next.plusWeeks(1)
    }

    return next.toInstant().toEpochMilli() - nowMillis
}
