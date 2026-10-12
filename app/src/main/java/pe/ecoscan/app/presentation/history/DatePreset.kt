package pe.ecoscan.app.presentation.history

// Opciones rápidas del filtro por fecha. CUSTOM usa el rango elegido en el selector de fechas.
enum class DatePreset {
    ALL,
    LAST_7_DAYS,
    LAST_30_DAYS,
    CUSTOM
}

// Rango de fechas en epoch millis locales, ambos extremos inclusivos.
data class DateRange(val fromMillis: Long, val toMillis: Long)
//Arreglo de DatePreset.kt