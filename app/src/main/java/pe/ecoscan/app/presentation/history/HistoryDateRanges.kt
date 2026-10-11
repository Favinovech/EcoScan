package pe.ecoscan.app.presentation.history

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

// Rango "últimos N días" contando hoy: desde las 00:00 de hace (N - 1) días hasta ahora.
fun lastDaysRange(days: Int, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): DateRange {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return DateRange(
        fromMillis = today.minusDays((days - 1).toLong()).startOfDayMillis(zone),
        toMillis = nowMillis
    )
}

// El selector de fechas de Material devuelve la medianoche UTC de cada día elegido.
// Se convierte al día completo en la zona horaria del dispositivo: desde las 00:00 del
// primer día hasta las 23:59:59.999 del último. Si solo se elige un día, el rango es ese día.
fun pickerRangeToLocal(
    startUtcMillis: Long,
    endUtcMillis: Long?,
    zone: ZoneId = ZoneId.systemDefault()
): DateRange {
    val start = Instant.ofEpochMilli(startUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val end = endUtcMillis
        ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
        ?: start
    val (first, last) = if (end.isBefore(start)) end to start else start to end
    return DateRange(
        fromMillis = first.startOfDayMillis(zone),
        toMillis = last.plusDays(1).startOfDayMillis(zone) - 1
    )
}

private fun LocalDate.startOfDayMillis(zone: ZoneId): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()