package pe.ecoscan.app.data.remote.dto

// Respuesta de Overpass API (OpenStreetMap). Todos los campos son opcionales porque los
// datos de OSM son colaborativos y pueden venir incompletos.
data class OverpassResponseDto(
    val elements: List<OverpassElementDto>? = null
)

// Un elemento puede ser un "node" (trae lat/lon) o un "way" (trae center con lat/lon).
data class OverpassElementDto(
    val type: String? = null,
    val id: Long? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenterDto? = null,
    val tags: Map<String, String>? = null
)

data class OverpassCenterDto(
    val lat: Double? = null,
    val lon: Double? = null
)