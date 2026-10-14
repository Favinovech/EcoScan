package pe.ecoscan.app.domain.model

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// Coordenada geográfica en grados. El dominio no usa tipos de Android ni de Google Maps.
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) {
    // Distancia en metros en línea recta hasta otro punto (fórmula de Haversine).
    fun distanceTo(other: GeoPoint): Double {
        val dLat = (other.latitude - latitude).toRadians()
        val dLon = (other.longitude - longitude).toRadians()
        val a = sin(dLat / 2).pow(2) +
                cos(latitude.toRadians()) * cos(other.latitude.toRadians()) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(min(1.0, sqrt(a)))
    }

    private fun Double.toRadians(): Double = this * PI / 180.0

    companion object {
        const val EARTH_RADIUS_METERS = 6_371_000.0
    }
}