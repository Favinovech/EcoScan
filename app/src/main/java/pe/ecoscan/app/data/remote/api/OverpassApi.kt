package pe.ecoscan.app.data.remote.api

import pe.ecoscan.app.data.remote.dto.OverpassResponseDto
import pe.ecoscan.app.domain.model.GeoPoint
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

// Overpass API (OpenStreetMap): puntos de reciclaje cercanos (HU07). Es gratuita y no
// requiere clave. El servidor se recibe como parametro (@Url) porque los servidores publicos
// se saturan seguido: el repositorio prueba varios en orden hasta que uno responda.
interface OverpassApi {

    // POST con el campo "data", igual que hace Overpass Turbo: evita problemas de codificacion
    // de la consulta en la URL y es lo que recomienda la documentacion de Overpass.
    @FormUrlEncoded
    @POST
    suspend fun getCollectionPoints(
        @Url endpoint: String,
        @Field("data") query: String
    ): OverpassResponseDto

    companion object {
        private const val MAX_ELEMENTS = 200
        private const val SERVER_TIMEOUT_SECONDS = 25

        // Servidores publicos de Overpass, en orden de prioridad.
        val ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.private.coffee/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter"
        )

        // Consulta en Overpass QL. Incluye amenity=recycling (el tag estandar) y tambien
        // estaciones de transferencia de residuos y elementos con recycling_type, porque en
        // Lima muchos puntos estan mapeados con otras etiquetas. "nwr" = nodos, vias y relaciones.
        // "out center tags" devuelve ademas el centro de las areas y sus etiquetas
        // (nombre, materiales aceptados, horario, direccion).
        fun buildQuery(center: GeoPoint, radiusMeters: Int): String {
            val around = "around:$radiusMeters,${center.latitude},${center.longitude}"
            return "[out:json][timeout:$SERVER_TIMEOUT_SECONDS];" +
                    "(nwr[\"amenity\"=\"recycling\"]($around);" +
                    "nwr[\"amenity\"=\"waste_transfer_station\"]($around);" +
                    "nwr[\"recycling_type\"]($around););" +
                    "out center tags $MAX_ELEMENTS;"
        }
    }
}