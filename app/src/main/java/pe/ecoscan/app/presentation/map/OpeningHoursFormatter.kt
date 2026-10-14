package pe.ecoscan.app.presentation.map

import java.util.Locale

// OpenStreetMap guarda el horario en inglés abreviado ("Mo-Fr 09:00-18:00; Sa 09:00-13:00").
// Se traduce lo más común para mostrarlo en español; lo que no se reconoce se deja tal cual.
private val DAY_NAMES = mapOf(
    "Mo" to "Lun", "Tu" to "Mar", "We" to "Mié", "Th" to "Jue",
    "Fr" to "Vie", "Sa" to "Sáb", "Su" to "Dom"
)

fun formatOpeningHours(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value == "24/7") return "Abierto las 24 horas"

    var result = value
    DAY_NAMES.forEach { (english, spanish) ->
        result = result.replace(Regex("\\b$english\\b"), spanish)
    }
    return result
        .replace(Regex("\\boff\\b"), "cerrado")
        .replace(Regex("\\bPH\\b"), "feriados")
        .replace(";", " ·")
        .replace(Regex("\\s+"), " ")
}

// 350 -> "350 m"; 1250 -> "1.3 km" (con coma decimal en español)
fun formatDistance(meters: Int, locale: Locale = Locale.getDefault()): String =
    if (meters < 1_000) "$meters m" else String.format(locale, "%.1f km", meters / 1000.0)