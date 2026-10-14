package pe.ecoscan.app.domain.repository

import pe.ecoscan.app.domain.model.GeoPoint

interface LocationRepository {

    // Ubicación actual del dispositivo. Falla si falta el permiso, si el GPS está apagado
    // o si no se obtuvo una posición a tiempo; el mensaje del error es apto para el usuario.
    suspend fun getCurrentLocation(): Result<GeoPoint>
}