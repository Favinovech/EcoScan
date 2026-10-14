package pe.ecoscan.app.domain.model

// Punto de acopio (HU07). Los datos opcionales vienen de OpenStreetMap y muchos puntos no
// los tienen: name, address y openingHours pueden ser null y acceptedMaterials puede estar
// vacío cuando el punto no especifica qué recibe.
data class CollectionPoint(
    val id: String,
    val name: String?,
    val location: GeoPoint,
    val address: String?,
    val openingHours: String?,
    val acceptedMaterials: Set<WasteCategory>,
    // Distancia en metros desde el lugar donde se hizo la búsqueda.
    val distanceMeters: Int
)