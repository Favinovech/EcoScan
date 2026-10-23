package pe.ecoscan.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory

class FilterCollectionPointsUseCaseTest {

    private val useCase = FilterCollectionPointsUseCase()

    private fun point(id: String, distance: Int, vararg materials: WasteCategory) = CollectionPoint(
        id = id,
        name = id,
        location = GeoPoint(0.0, 0.0),
        address = null,
        openingHours = null,
        acceptedMaterials = materials.toSet(),
        distanceMeters = distance
    )

    private val nearPlastic = point("a", 500, WasteCategory.PLASTIC)
    private val midGlassPlastic = point("b", 1_500, WasteCategory.GLASS, WasteCategory.PLASTIC)
    private val farGlass = point("c", 4_000, WasteCategory.GLASS)
    private val unknownMaterials = point("d", 800)
    private val points = listOf(nearPlastic, midGlassPlastic, farGlass, unknownMaterials)

    @Test
    fun `sin filtros devuelve todos los puntos`() {
        assertEquals(points, useCase(points, material = null, maxDistanceMeters = null))
    }

    @Test
    fun `filtra por material aceptado`() {
        val result = useCase(points, material = WasteCategory.PLASTIC, maxDistanceMeters = null)

        assertEquals(listOf(nearPlastic, midGlassPlastic), result)
    }

    @Test
    fun `con un material elegido se descartan los puntos que no especifican materiales`() {
        val result = useCase(points, material = WasteCategory.GLASS, maxDistanceMeters = null)

        assertEquals(listOf(midGlassPlastic, farGlass), result)
    }

    @Test
    fun `filtra por distancia maxima con extremo inclusivo`() {
        val result = useCase(points, material = null, maxDistanceMeters = 1_500)

        assertEquals(listOf(nearPlastic, midGlassPlastic, unknownMaterials), result)
    }

    @Test
    fun `combina material y distancia`() {
        val result = useCase(points, material = WasteCategory.GLASS, maxDistanceMeters = 2_000)

        assertEquals(listOf(midGlassPlastic), result)
    }

    @Test
    fun `sin coincidencias devuelve lista vacia`() {
        assertTrue(useCase(points, material = WasteCategory.METAL, maxDistanceMeters = null).isEmpty())
    }
}