package pe.ecoscan.app.data.remote.api

import pe.ecoscan.app.data.remote.dto.OverpassResponseDto
import pe.ecoscan.app.domain.model.GeoPoint
import retrofit2.http.GET
import retrofit2.http.Query

// Overpass API (OpenStreetMap): puntos de reciclaje cercanos (HU07). Es gratuita y no
// requiere clave. La URL es absoluta porque el Retrofit compartido apunta a Open Food Facts.
interface OverpassApi {

    @GET("https://overpass-api.de/api/interpreter")
    suspend fun getCollectionPoints(@Query("data") query: String): OverpassResponseDto

    companion object {
        private const val MAX_ELEMENTS = 200

        // Consulta en Overpass QL: nodos y áreas con amenity=recycling dentro del radio.
        // "out center tags" devuelve además el centro de las áreas y sus etiquetas
        // (nombre, materiales aceptados, horario, dirección).
        fun buildQuery(center: GeoPoint, radiusMeters: Int): String {
            val around = "around:$radiusMeters,${center.latitude},${center.longitude}"
            return "[out:json][timeout:10];" +
                    "(node[\"amenity\"=\"recycling\"]($around);" +
                    "way[\"amenity\"=\"recycling\"]($around););" +
                    "out center tags $MAX_ELEMENTS;"
        }
    }
}