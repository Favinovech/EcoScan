package pe.ecoscan.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoPointTest {

    private val plazaMayor = GeoPoint(-12.0464, -77.0428)
    private val parqueKennedy = GeoPoint(-12.1219, -77.0297)

    @Test
    fun `distancia entre dos puntos de Lima es la esperada`() {
        // Plaza Mayor - Parque Kennedy (Miraflores): unos 8.5 km en línea recta.
        assertEquals(8_515.0, plazaMayor.distanceTo(parqueKennedy), 50.0)
    }

    @Test
    fun `la distancia de un punto a si mismo es cero`() {
        assertEquals(0.0, plazaMayor.distanceTo(plazaMayor), 0.001)
    }

    @Test
    fun `la distancia es simetrica`() {
        assertEquals(plazaMayor.distanceTo(parqueKennedy), parqueKennedy.distanceTo(plazaMayor), 0.001)
    }

    @Test
    fun `un cambio chico de longitud da la distancia en metros esperada`() {
        // 0.009 grados de longitud a esta latitud son unos 979 m.
        val east = GeoPoint(plazaMayor.latitude, plazaMayor.longitude + 0.009)

        assertEquals(979.0, plazaMayor.distanceTo(east), 5.0)
    }
}