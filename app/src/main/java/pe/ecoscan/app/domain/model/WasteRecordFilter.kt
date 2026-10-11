package pe.ecoscan.app.domain.model

// Filtros del historial (HU06): por categoría y por rango de fechas.
// fromMillis y toMillis son epoch millis inclusivos; null significa "sin límite".
data class WasteRecordFilter(
    val category: WasteCategory? = null,
    val fromMillis: Long? = null,
    val toMillis: Long? = null
) {
    val isActive: Boolean
        get() = category != null || fromMillis != null || toMillis != null

    fun matches(record: WasteRecord): Boolean =
        (category == null || record.category == category) &&
                (fromMillis == null || record.recordedAt >= fromMillis) &&
                (toMillis == null || record.recordedAt <= toMillis)
}