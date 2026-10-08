package pe.ecoscan.app.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.ecoscan.app.data.local.entity.CachedProductEntity
import pe.ecoscan.app.data.remote.dto.ProductDto

class ProductMapperTest {

    @Test
    fun `ProductDto a domain con todos los campos presentes`() {
        val dto = ProductDto(
            productName = "Agua Mineral",
            brands = "San Luis",
            packaging = "Botella de plástico",
            packagingTags = listOf("en:plastic"),
            categoriesTags = listOf("en:beverages"),
            imageFrontSmallUrl = "https://images.example/agua.jpg"
        )

        val product = dto.toDomain(barcode = "7751271014013")

        assertEquals("7751271014013", product.barcode)
        assertEquals("Agua Mineral", product.name)
        assertEquals("San Luis", product.brand)
        assertEquals(listOf("Plástico"), product.packagingMaterials)
        assertEquals("https://images.example/agua.jpg", product.imageUrl)
    }

    @Test
    fun `ProductDto a domain con campos opcionales ausentes no falla`() {
        val dto = ProductDto()

        val product = dto.toDomain(barcode = "0000000000000")

        assertEquals("0000000000000", product.barcode)
        assertNull(product.name)
        assertNull(product.brand)
        assertNull(product.imageUrl)
        assertEquals(emptyList<String>(), product.packagingMaterials)
    }

    @Test
    fun `domain a entity y de vuelta a domain conserva los materiales`() {
        val dto = ProductDto(productName = "Yogurt", packaging = "Tetra Pak")
        val product = dto.toDomain(barcode = "123")

        val entity = product.toEntity(cachedAt = 1_000L)
        val roundTrip = entity.toDomain()

        assertEquals(product.packagingMaterials, roundTrip.packagingMaterials)
        assertEquals(product, roundTrip)
    }

    @Test
    fun `entity sin materiales reconocidos mapea a lista vacia`() {
        val entity = CachedProductEntity(
            barcode = "999",
            name = null,
            brand = null,
            packagingMaterials = "",
            imageUrl = null,
            disposalHint = "No se reconoció el material de empaque.",
            cachedAt = 1_000L
        )

        val product = entity.toDomain()

        assertEquals(emptyList<String>(), product.packagingMaterials)
    }
}
