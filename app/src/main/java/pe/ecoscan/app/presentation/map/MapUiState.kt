package pe.ecoscan.app.presentation.map

import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory

data class MapUiState(
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = false,
    val userLocation: GeoPoint? = null,
    // Todos los puntos encontrados dentro del radio de búsqueda.
    val allPoints: List<CollectionPoint> = emptyList(),
    // Los que se dibujan en el mapa: ya filtrados por material y distancia.
    val visiblePoints: List<CollectionPoint> = emptyList(),
    val selectedMaterial: WasteCategory? = null,
    val maxDistanceMeters: Int = DEFAULT_MAX_DISTANCE_METERS,
    // Punto tocado en el mapa: se muestra su ficha.
    val selectedPoint: CollectionPoint? = null,
    // true cuando los puntos vienen de lo guardado en el dispositivo (sin conexión).
    val isOfflineData: Boolean = false,
    // Error que impide mostrar el mapa (GPS apagado, sin ubicación): pantalla con "Reintentar".
    val locationError: String? = null,
    // Mensaje de una sola vez (Snackbar): fallo de red con el mapa ya visible.
    val errorMessage: String? = null
) {
    companion object {
        // Radio con el que se consulta la API. Los filtros de distancia se aplican sobre estos
        // resultados, sin volver a llamar a la red.
        const val SEARCH_RADIUS_METERS = 5_000
        const val DEFAULT_MAX_DISTANCE_METERS = SEARCH_RADIUS_METERS
        val DISTANCE_OPTIONS_METERS = listOf(1_000, 2_000, 5_000)
    }
}