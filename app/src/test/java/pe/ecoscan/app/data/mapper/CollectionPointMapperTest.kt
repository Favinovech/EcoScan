package pe.ecoscan.app.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.data.local.entity.CachedCollectionPointEntity
import pe.ecoscan.app.data.remote.dto.OverpassCenterDto
import pe.ecoscan.app.data.remote.dto.OverpassElementDto
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory

class CollectionPointMapperTest {

    private val origin = GeoPoint(-12.1, -77.0)

    private fun node(tags: Map<String, String>) = OverpassElementDto(
        type = "node", id = 123L, lat = -12.1, lon = -77.0, tags = tags
    )

    @Test
    fun `un nodo con etiquetas se convierte en punto de acopio`() {
        val point = node(
            mapOf(
                "name" to "Eco Punto",
                "recycling:plastic" to "yes",
                "recycling:glass" to "yes",
                "recycling:paper" to "no",
                "addr:street" to "Av. Larco",
                "addr:housenumber" to "345",
                "addr:suburb" to "Miraflores",
                "opening_hours" to "Mo-Fr 09:00-18:00"
            )
        ).toDomain(origin)!!

        assertEquals("node/123", point.id)
        assertEquals("Eco Punto", point.name)
        assertEquals("Av. Larco 345, Miraflores", point.address)
        assertEquals("Mo-Fr 09:00-18:00", point.openingHours)
        assertEquals(setOf(WasteCategory.PLASTIC, WasteCategory.GLASS), point.acceptedMaterials)
        assertEquals(0, point.distanceMeters)
    }

    @Test
    fun `un area usa las coordenadas de su centro`() {
        val point = OverpassElementDto(
            type = "way", id = 5L, center = OverpassCenterDto(lat = -12.1, lon = -77.0), tags = emptyMap()
        ).toDomain(origin)!!

        assertEquals("way/5", point.id)
        assertEquals(GeoPoint(-12.1, -77.0), point.location)
    }

    @Test
    fun `sin coordenadas o sin id el elemento se descarta`() {
        assertNull(OverpassElementDto(type = "node", id = 1L, tags = emptyMap()).toDomain(origin))
        assertNull(OverpassElementDto(type = "node", lat = -12.1, lon = -77.0).toDomain(origin))
    }

    @Test
    fun `sin nombre se usa el operador y un nombre en blanco cuenta como ausente`() {
        assertEquals("Municipalidad", node(mapOf("operator" to "Municipalidad")).toDomain(origin)!!.name)
        assertNull(node(mapOf("name" to "   ")).toDomain(origin)!!.name)
    }

    @Test
    fun `sin etiquetas de direccion ni horario quedan en null y sin materiales`() {
        val point = node(emptyMap()).toDomain(origin)!!

        assertNull(point.address)
        assertNull(point.openingHours)
        assertTrue(point.acceptedMaterials.isEmpty())
    }

    @Test
    fun `las pilas y los aceites se consideran residuos peligrosos`() {
        val point = node(mapOf("recycling:batteries" to "yes", "recycling:cooking_oil" to "yes")).toDomain(origin)!!

        assertEquals(setOf(WasteCategory.HAZARDOUS), point.acceptedMaterials)
    }

    @Test
    fun `addr full tiene prioridad sobre las otras etiquetas de direccion`() {
        val point = node(mapOf("addr:full" to "Jr. Unión 100, Lima", "addr:street" to "Otra")).toDomain(origin)!!

        assertEquals("Jr. Unión 100, Lima", point.address)
    }

    @Test
    fun `la distancia se calcula desde el origen de la busqueda`() {
        val point = OverpassElementDto(type = "node", id = 1L, lat = -12.1, lon = -77.0 + 0.009).toDomain(origin)!!

        // 0.009 grados de longitud a esta latitud son unos 980 m.
        assertEquals(980.0, point.distanceMeters.toDouble(), 5.0)
    }

    @Test
    fun `ida y vuelta por la entidad conserva los datos`() {
        val original = node(
            mapOf("name" to "Eco Punto", "recycling:metal" to "yes", "recycling:paper" to "yes")
        ).toDomain(origin)!!

        val restored = original.toEntity(cachedAt = 1_000L).toDomain(origin)

        assertEquals(original, restored)
    }

    @Test
    fun `la entidad ignora materiales desconocidos y soporta la lista vacia`() {
        val entity = CachedCollectionPointEntity(
            id = "node/1", name = null, latitude = -12.1, longitude = -77.0,
            address = null, openingHours = null, acceptedMaterials = "", cachedAt = 0L
        )

        assertTrue(entity.toDomain(origin).acceptedMaterials.isEmpty())
        assertEquals(
            setOf(WasteCategory.GLASS),
            entity.copy(acceptedMaterials = "GLASS|INEXISTENTE").toDomain(origin).acceptedMaterials
        )
    }
}